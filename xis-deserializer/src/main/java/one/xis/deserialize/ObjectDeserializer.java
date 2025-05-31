package one.xis.deserialize;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import lombok.RequiredArgsConstructor;
import one.xis.UserContext;
import one.xis.context.XISComponent;
import one.xis.utils.lang.ClassUtils;
import one.xis.utils.lang.FieldUtil;
import one.xis.validation.Mandatory;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static one.xis.deserialize.DefaultDeserializationErrorType.MISSING_MANDATORY_PROPERTY;

@XISComponent
@RequiredArgsConstructor
class ObjectDeserializer implements JsonDeserializer<Object> {

    @Override
    public boolean matches(JsonToken token, AnnotatedElement target) {
        return token.equals(JsonToken.BEGIN_OBJECT);
    }

    @Override
    public Optional<Object> deserialize(JsonReader reader,
                                        String path,
                                        AnnotatedElement target,
                                        UserContext userContext,
                                        MainDeserializer mainDeserializer,
                                        PostProcessingResults results) throws IOException {
        var objectType = getType(target);
        if (objectType.isRecord()) {
            try {
                return deserializeRecord(objectType, reader, path, target, userContext, mainDeserializer, results);
            } catch (IOException | IllegalAccessException | InstantiationException | NoSuchMethodException |
                     InvocationTargetException e) {
                throw new RuntimeException("Error deserializing record: " + objectType.getName(), e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            return deserializeObject(objectType, reader, path, userContext, mainDeserializer, results);
        }
    }

    private Optional<Object> deserializeObject(Class<?> objectType,
                                               JsonReader reader,
                                               String path,
                                               UserContext userContext,
                                               MainDeserializer mainDeserializer,
                                               PostProcessingResults results) throws IOException {
        var o = ClassUtils.newInstance(objectType);
        var mandatoryFields = getMandatoryFields(objectType);
        var fieldMap = fieldMap(objectType);
        reader.beginObject();
        while (reader.hasNext()) {
            var name = reader.nextName();
            if (!fieldMap.containsKey(name)) {
                throw new IllegalArgumentException("no field named '" + name + "' present in " + o.getClass());
            }
            var field = fieldMap.get(name);
            mandatoryFields.remove(field);
            mainDeserializer.deserialize(reader, path(path, field), field, userContext, results)
                    .ifPresent(fieldValue -> FieldUtil.setFieldValue(o, field, fieldValue));
            if (reader.peek().equals(JsonToken.END_OBJECT)) {
                reader.endObject();
                break;
            }
        }
        // We need this in case the field is not present in the JSON:
        mandatoryFields.forEach(field -> {
            var context = new DeserializationContext(path(path, field), field, Mandatory.class, UserContext.getInstance());
            results.add(new InvalidValueError(context, MISSING_MANDATORY_PROPERTY.getMessageKey(), MISSING_MANDATORY_PROPERTY.getGlobalMessageKey(), o));
        });
        return Optional.of(o);
    }


    private Optional<Object> deserializeRecord(Class<?> clazz,
                                               JsonReader reader,
                                               String path,
                                               AnnotatedElement target,
                                               UserContext userContext,
                                               MainDeserializer mainDeserializer,
                                               PostProcessingResults results) throws IOException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        Map<String, RecordComponent> components = Arrays.stream(clazz.getRecordComponents())
                .collect(Collectors.toMap(RecordComponent::getName, Function.identity()));

        Map<String, Object> values = new HashMap<>();

        reader.beginObject();
        while (reader.peek() != JsonToken.END_OBJECT) {
            String name = reader.nextName();
            RecordComponent component = components.get(name);
            if (component == null) {
                reader.skipValue();
                continue;
            }
            Object value = mainDeserializer.deserialize(reader, path, component, userContext, results).orElse(null);
            values.put(name, value);
        }
        reader.endObject();

        Object[] args = new Object[components.size()];
        Class<?>[] types = new Class<?>[components.size()];
        int i = 0;
        for (RecordComponent component : clazz.getRecordComponents()) {
            if (!values.containsKey(component.getName())) {
                throw new IllegalStateException("Missing required record component: " + component.getName());
            }
            args[i] = values.get(component.getName());
            types[i] = component.getType();
            i++;
        }

        Constructor<?> constructor = clazz.getDeclaredConstructor(types);
        constructor.setAccessible(true);
        return Optional.of(constructor.newInstance(args));
    }


    private Set<Field> getMandatoryFields(Class<?> objectType) {
        return FieldUtil.getAllFields(objectType).stream()
                .filter(f -> f.isAnnotationPresent(Mandatory.class))
                .collect(Collectors.toSet());
    }

    @Override
    public DeserializerPriority getPriority() {
        return DeserializerPriority.FRAMEWORK_LOW;
    }

    private Map<String, Field> fieldMap(Class<?> objectType) {
        return FieldUtil.getAllFields(objectType).stream().collect(Collectors.toMap(Field::getName, Function.identity()));
    }

    private String path(String path, Field field) {
        return path + "/" + getName(field);
    }


}
