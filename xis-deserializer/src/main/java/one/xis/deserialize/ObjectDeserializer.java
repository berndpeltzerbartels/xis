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
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
                                        Collection<ReportedError> failed) throws IOException {
        var objectType = getType(target);
        var o = ClassUtils.newInstance(objectType);
        var mandatorFields = getMandatoryFields(objectType);
        var fieldMap = fieldMap(objectType);
        reader.beginObject();
        while (reader.hasNext()) {
            var name = reader.nextName();
            if (!fieldMap.containsKey(name)) {
                throw new IllegalArgumentException("no field named '" + name + "' present in " + o.getClass());
            }
            var field = fieldMap.get(name);
            mandatorFields.remove(field);
            mainDeserializer.deserialize(reader, path(path, field), field, userContext, failed)
                    .ifPresent(fieldValue -> FieldUtil.setFieldValue(o, field, fieldValue));
            if (reader.peek().equals(JsonToken.END_OBJECT)) {
                reader.endObject();
                break;
            }
        }
        // We need this in case the field is not present in the JSON:
        mandatorFields.forEach(field -> {
            var context = new ReportedErrorContext(path(path, field), field, Mandatory.class, UserContext.getInstance());
            failed.add(new ReportedError(context, MISSING_MANDATORY_PROPERTY.getMessageKey(), MISSING_MANDATORY_PROPERTY.getGlobalMessageKey()));
        });
        return Optional.of(o);
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
