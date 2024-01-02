package one.xis.parameter;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.utils.lang.ClassUtils;
import one.xis.utils.lang.CollectionUtils;
import one.xis.utils.lang.FieldUtil;
import one.xis.validation.Validation;
import one.xis.validation.ValidatorResultElement;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.time.ZoneId;
import java.util.*;

@XISComponent
@RequiredArgsConstructor
class ParameterDeserializerImpl implements ParameterDeserializer {

    private final Validation validation;
    private final Collection<JsonDeserializer<Object>> deserializers;
    private final Map<Target, JsonDeserializer<Object>> deserializerCache = new HashMap<>();

    @Override
    public Optional<Object> deserialize(String json, Field field, ValidatorResultElement parameterResult, Locale locale, ZoneId zoneId) throws IOException {
        return deserialze(json, new TargetField(field), parameterResult, locale, zoneId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<Object> deserialize(String paramValue, Parameter parameter, ValidatorResultElement validatorResultElement, Locale locale, ZoneId zoneId) throws IOException {
        if (paramValue == null) {
            if (Collection.class.isAssignableFrom(parameter.getType())) {
                var collection = CollectionUtils.emptyInstance((Class<Collection<?>>) parameter.getType());
                validation.validateBeforeAssignment(parameter.getType(), collection, validatorResultElement);
                return Optional.of(collection);
            }
            return Optional.empty();
        } else if (String.class.isAssignableFrom(parameter.getType())) {
            validation.validateBeforeAssignment(String.class, "", validatorResultElement);
            return Optional.of(paramValue);
        }
        return deserialze(paramValue, new TargetParameter(parameter), validatorResultElement, locale, zoneId);
    }


    private Optional<Object> deserialze(String json, Target target, ValidatorResultElement parameterResult, Locale locale, ZoneId zoneId) throws IOException {
        var reader = new JsonReader(new StringReader(json));
        reader.setLenient(true);
        return read(reader, target, parameterResult, locale, zoneId);
    }


    private Optional<Object> read(JsonReader reader, Target target, ValidatorResultElement result, Locale locale, ZoneId zoneId) throws IOException {
        var context = new ParameterDeserializationContext(result, locale, zoneId);
        return read(reader, target, new ParameterDeserializationContext(result, context));
    }

    private Optional<Object> read(JsonReader reader, Target target, ParameterDeserializationContext context) throws IOException {
        Optional<Object> value;
        if (reader.peek() == JsonToken.STRING || reader.peek() == JsonToken.NUMBER) {
            var deserializer = getDeserializer(target, reader.peek());
            value = deserializer.deserialize(reader, target, context);
        } else if (reader.peek() == JsonToken.BEGIN_ARRAY) {
            if (Collection.class.isAssignableFrom(target.getType())) {
                value = Optional.of(deserializeArrayToCollection(reader, target, context));
            } else if (target.getType().isArray()) {
                value = Optional.of(deserializeArrayToArray(reader, target, context));
            } else {
                throw new IllegalStateException();
            }
        } else if (reader.peek() == JsonToken.BEGIN_OBJECT) {
            value = Optional.of(deserializeObject(reader, target, context));
        } else {
            throw new IllegalStateException();
        }
        validation.validateAssignedValue(target.getType(), value, context.getValidatorResultElement());
        return value;
    }

    @NonNull
    private JsonDeserializer<Object> getDeserializer(Target target, JsonToken token) {
        if (deserializerCache.containsKey(target)) {
            return deserializerCache.get(target);
        }
        return deserializerCache.computeIfAbsent(target, t -> findDeserializer(t, token));
    }


    private JsonDeserializer<Object> findDeserializer(Target target, JsonToken token) {
        return deserializers.stream()
                .filter(deserializer -> deserializer.matchesTarget(target, token))
                .min(Comparator.comparing(JsonDeserializer::getPriority)).orElseThrow();
    }

    private Collection<Object> deserializeArrayToArray(JsonReader reader, Target target, ParameterDeserializationContext context) throws IOException {
        var list = new ArrayList<>();
        deserializeArray(reader, target, context, list);
        return list;
    }

    private Collection<Object> deserializeArrayToCollection(JsonReader reader, Target target, ParameterDeserializationContext context) throws IOException {
        var collection = CollectionUtils.emptyInstance((Class<? extends Collection<Object>>) target.getType());
        deserializeArray(reader, target, context, collection);
        return collection;
    }

    private void deserializeArray(JsonReader reader, Target target, ParameterDeserializationContext context, Collection<Object> collection) throws IOException {
        reader.beginArray();
        int index = 0;
        while (reader.hasNext()) {
            var result = context.getValidatorResultElement().childElement(target.getName(), index++);
            Target elementTarget;
            if (target.getElementType() instanceof ParameterizedType parameterizedType) {
                elementTarget = new ParameterizedTargetElement(target.getName(), parameterizedType);
            } else if (target.getElementType() instanceof Class<?> clazz) {
                elementTarget = new ClassTargetElement(target.getName(), clazz);
            } else {
                throw new IllegalStateException();
            }
            read(reader, elementTarget, new ParameterDeserializationContext(result, context)).ifPresent(collection::add);
        }
        reader.endArray();
    }

    private Object deserializeObject(JsonReader reader, Target target, ParameterDeserializationContext context) throws IOException {
        var o = ClassUtils.newInstance(target.getType());
        readObjectFields(reader, o, context);
        return o;
    }

    private void readObjectFields(JsonReader reader, Object o, ParameterDeserializationContext context) throws IOException {
        reader.beginObject();
        while (reader.hasNext()) {
            var name = reader.nextName();
            var field = FieldUtil.getField(o.getClass(), name);
            if (field != null) {
                var result = context.getValidatorResultElement().childElement(name, 0);
                Object value = null;
                try {
                    var targetField = new TargetField(field);
                    read(reader, targetField, new ParameterDeserializationContext(result, context)).ifPresent(v -> {
                        validation.validateBeforeAssignment(targetField.getType(), v, result);
                        if (!result.hasError()) {
                            FieldUtil.setFieldValue(o, field, v);
                        }
                    });
                } catch (IllegalArgumentException e) {
                    validation.assignmentError(field.getType(), value, result);
                }
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
    }

}
