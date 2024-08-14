package one.xis.deserialize;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import lombok.NonNull;
import lombok.SneakyThrows;
import one.xis.FormData;
import one.xis.ModelData;
import one.xis.UserContext;
import one.xis.context.XISComponent;
import one.xis.validation.Mandatory;

import java.io.StringReader;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static one.xis.deserialize.DefaultDeserializationErrorType.CONVERSION_ERROR;
import static one.xis.deserialize.DefaultDeserializationErrorType.MISSING_MANDATORY_PROPERTY;

@XISComponent
@SuppressWarnings("rawtypes")
public class MainDeserializer {

    private final List<JsonDeserializer<?>> deserializers;
    private DeserializationPostProcessing deserializationPostProcessing;

    public MainDeserializer(@NonNull List<JsonDeserializer<?>> deserializers, @NonNull DeserializationPostProcessing deserializationPostProcessing) {
        this.deserializers = deserializers;
        this.deserializationPostProcessing = deserializationPostProcessing;
        deserializers.sort(Comparator.reverseOrder());
    }


    public Object deserialize(@NonNull String value, @NonNull AnnotatedElement target, @NonNull UserContext userContext, @NonNull Collection<ReportedError> failed) {
        var reader = new JsonReader(new StringReader(value));
        reader.setLenient(true);
        var path = "/" + getName(target);
        return deserialize(reader, path, target, userContext, failed).orElse(null);
    }

    @SneakyThrows
    Optional<?> deserialize(JsonReader reader, String path, AnnotatedElement target, UserContext userContext, Collection<ReportedError> failed) {
        if (reader.peek().equals(JsonToken.NULL)) {
            reader.nextNull();
            if (target.isAnnotationPresent(Mandatory.class)) {
                var context = new ReportedErrorContext(path, target, Mandatory.class, UserContext.getInstance());
                failed.add(new ReportedError(context, MISSING_MANDATORY_PROPERTY.getMessageKey(), MISSING_MANDATORY_PROPERTY.getGlobalMessageKey()));
            }
            return Optional.empty();
        }
        try {
            var value = getDeserializer(reader, target).deserialize(reader, path, target, userContext, this, failed);
            value.ifPresent(o -> deserializationPostProcessing.postProcess(path, o, target, userContext, failed));
            return value;
        } catch (DeserializationException e) {
            var context = new ReportedErrorContext(path, target, NoAnnotation.class, UserContext.getInstance());
            failed.add(new ReportedError(context, CONVERSION_ERROR.getMessageKey(), CONVERSION_ERROR.getGlobalMessageKey()));
            return Optional.empty();
        }

    }

    private JsonDeserializer<?> getDeserializer(JsonReader reader, AnnotatedElement target) {
        return deserializers.stream()
                .filter(d -> matches(d, reader, target))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No deserializer found for " + target));
    }

    @SneakyThrows
    private boolean matches(JsonDeserializer deserializer, JsonReader reader, AnnotatedElement target) {
        return deserializer.matches(reader.peek(), target);
    }

    private String getName(AnnotatedElement target) {
        if (target.isAnnotationPresent(ModelData.class)) {
            return target.getAnnotation(ModelData.class).value();
        }
        if (target.isAnnotationPresent(FormData.class)) {
            return target.getAnnotation(FormData.class).value();
        }
        if (target instanceof Field field) {
            return field.getName();
        }
        if (target instanceof java.lang.reflect.Parameter parameter) {
            return parameter.getName();
        }
        if (target instanceof Class<?> clazz) {
            return clazz.getSimpleName();
        }
        throw new IllegalArgumentException("Unsupported target type: " + target.getClass());
    }
}
