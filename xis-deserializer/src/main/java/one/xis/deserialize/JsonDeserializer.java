package one.xis.deserialize;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import one.xis.UserContext;

import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.Optional;

public interface JsonDeserializer<T> extends Comparable<JsonDeserializer<?>> {

    boolean matches(JsonToken token, AnnotatedElement target);

    Optional<T> deserialize(JsonReader reader,
                            String path,
                            AnnotatedElement target,
                            UserContext userContext,
                            MainDeserializer mainDeserializer,
                            PostProcessingObjects results) throws DeserializationException, IOException;

    default String getName(AnnotatedElement target) {
        if (target instanceof Field field) {
            return field.getName();
        }
        if (target instanceof java.lang.reflect.Parameter parameter) {
            return parameter.getName();
        }
        throw new IllegalArgumentException("Unsupported target type: " + target.getClass());
    }

    default Class<?> getType(AnnotatedElement target) {
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

}
