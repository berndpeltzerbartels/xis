package one.xis.parameter;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

@XISComponent
@RequiredArgsConstructor
class ParameterDeserializerImpl implements ParameterDeserializer {

    private final Validation validation;
    private final Gson gson;

    @Override
    public Optional<Object> deserialize(String json, Field field, ValidatorResultElement parameterResult) throws IOException {
        return deserialze(json, new TargetField(field), parameterResult);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<Object> deserialize(String paramValue, Parameter parameter, ValidatorResultElement validatorResultElement) throws IOException {
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
        return deserialze(paramValue, new TargetParameter(parameter), validatorResultElement);
    }


    private Optional<Object> deserialze(String json, Target target, ValidatorResultElement parameterResult) throws IOException {
        var reader = new JsonReader(new StringReader(json));
        reader.setLenient(true);
        return read(reader, target, parameterResult);
    }


    private Optional<Object> read(JsonReader reader, Target target, ValidatorResultElement result) throws IOException {
        Optional<Object> value;
        if (reader.peek() == JsonToken.STRING || reader.peek() == JsonToken.NUMBER) {
            var adapter = gson.getAdapter(target.getType());
            return Optional.of(adapter.read(reader));
        } else if (reader.peek() == JsonToken.BEGIN_ARRAY) {
            if (Collection.class.isAssignableFrom(target.getType())) {
                value = Optional.of(deserializeArrayToCollection(reader, target, result));
            } else if (target.getType().isArray()) {
                value = Optional.of(deserializeArrayToArray(reader, target, result));
            } else {
                throw new IllegalStateException();
            }
        } else if (reader.peek() == JsonToken.BEGIN_OBJECT) {
            value = Optional.of(deserializeObject(reader, target, result));
        } else {
            throw new IllegalStateException();
        }
        validation.validateAssignedValue(target.getType(), value, result);
        return value;
    }


    private Collection<Object> deserializeArrayToArray(JsonReader reader, Target target, ValidatorResultElement result) throws IOException {
        var list = new ArrayList<>();
        deserializeArray(reader, target, result, list);
        return list;
    }

    private Collection<Object> deserializeArrayToCollection(JsonReader reader, Target target, ValidatorResultElement result) throws IOException {
        var collection = CollectionUtils.emptyInstance((Class<? extends Collection<Object>>) target.getType());
        deserializeArray(reader, target, result, collection);
        return collection;
    }

    private void deserializeArray(JsonReader reader, Target target, ValidatorResultElement parentResult, Collection<Object> collection) throws IOException {
        reader.beginArray();
        int index = 0;
        while (reader.hasNext()) {
            var result = parentResult.childElement(target.getName(), index++);
            Target elementTarget;
            if (target.getElementType() instanceof ParameterizedType parameterizedType) {
                elementTarget = new ParameterizedTargetElement(target.getName(), parameterizedType);
            } else if (target.getElementType() instanceof Class<?> clazz) {
                elementTarget = new ClassTargetElement(target.getName(), clazz);
            } else {
                throw new IllegalStateException();
            }
            read(reader, elementTarget, result).ifPresent(collection::add);
        }
        reader.endArray();
    }

    private Object deserializeObject(JsonReader reader, Target target, ValidatorResultElement resultElement) throws IOException {
        var o = ClassUtils.newInstance(target.getType());
        readObjectFields(reader, o, resultElement);
        return o;
    }

    private void readObjectFields(JsonReader reader, Object o, ValidatorResultElement parentResult) throws IOException {
        reader.beginObject();
        while (reader.hasNext()) {
            var name = reader.nextName();
            var field = FieldUtil.getField(o.getClass(), name);
            if (field != null) {
                var result = parentResult.childElement(name, 0);
                Object value = null;
                try {
                    var targetField = new TargetField(field);
                    read(reader, targetField, result).ifPresent(v -> {
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
