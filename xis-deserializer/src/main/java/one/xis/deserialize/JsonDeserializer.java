package one.xis.deserialize;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import lombok.NonNull;
import one.xis.UserContext;
import one.xis.validation.Mandatory;

import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static one.xis.deserialize.DefaultDeserializationErrorType.CONVERSION_ERROR;
import static one.xis.deserialize.DefaultDeserializationErrorType.MISSING_MANDATORY_PROPERTY;

public interface JsonDeserializer<T> extends Comparable<JsonDeserializer<?>> {

    boolean matches(JsonToken token, AnnotatedElement target);

    Optional<T> deserialize(JsonReader reader,
                            String path,
                            AnnotatedElement target,
                            UserContext userContext,
                            MainDeserializer mainDeserializer,
                            PostProcessingResults results) throws DeserializationException, IOException;

    default String getName(AnnotatedElement target) {
        if (target instanceof Field field) {
            return field.getName();
        }
        if (target instanceof java.lang.reflect.Parameter parameter) {
            return parameter.getName();
        }
        throw new IllegalArgumentException("Unsupported target type: " + target.getClass());
    }

    default Class<?> getType(@NonNull AnnotatedElement target) {
        if (target instanceof Field field) {
            return field.getType();
        }
        if (target instanceof java.lang.reflect.Parameter parameter) {
            return parameter.getType();
        }
        if (target instanceof Class<?> classTarget) {
            return classTarget;
        }
        throw new IllegalArgumentException("Unsupported target type: " + target.getClass());
    }

    default DeserializerPriority getPriority() {
        return DeserializerPriority.CUSTOM_NORMAL;
    }

    @Override
    default int compareTo(JsonDeserializer<?> o) {
        return getPriority().compareTo(o.getPriority());
    }


    default void checkMandatory(List<?> values, AnnotatedElement target, PostProcessingResults postProcessingResults, String path) {
        if (target.isAnnotationPresent(Mandatory.class) && values.isEmpty()) {
            var context = new DeserializationContext(path, target, Mandatory.class, UserContext.getInstance());
            postProcessingResults.add(new InvalidValueError(context, MISSING_MANDATORY_PROPERTY.getMessageKey(), MISSING_MANDATORY_PROPERTY.getGlobalMessageKey(), values));
        }
    }

    default void handleDeserializationError(List<?> values, String path, AnnotatedElement target, PostProcessingResults postProcessingResults) {
        var context = new DeserializationContext(path, target, NoAnnotation.class, UserContext.getInstance());
        postProcessingResults.add(new InvalidValueError(context, CONVERSION_ERROR.getMessageKey(), CONVERSION_ERROR.getGlobalMessageKey(), values));
    }

}
