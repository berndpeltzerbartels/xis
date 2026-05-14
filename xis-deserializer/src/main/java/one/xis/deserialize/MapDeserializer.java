package one.xis.deserialize;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import lombok.NonNull;
import one.xis.UserContext;
import one.xis.context.Component;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Component
@SuppressWarnings({"rawtypes", "unchecked"})
class MapDeserializer implements JsonDeserializer<Map> {

    @Override
    public boolean matches(@NonNull JsonToken token, @NonNull AnnotatedElement target) {
        return Map.class.isAssignableFrom(getType(target));
    }

    @Override
    public Optional<Map> deserialize(JsonReader reader,
                                     String path,
                                     AnnotatedElement target,
                                     UserContext userContext,
                                     MainDeserializer mainDeserializer,
                                     PostProcessingResults results) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull();
            return Optional.of(createMap(getType(target)));
        }
        var map = createMap(getType(target));
        var valueTarget = valueTarget(target);
        reader.beginObject();
        while (reader.hasNext()) {
            var key = reader.nextName();
            var value = mainDeserializer.deserialize(reader, path(path, key), valueTarget, userContext, results).orElse(null);
            map.put(key, value);
        }
        reader.endObject();
        return Optional.of(map);
    }

    @Override
    public DeserializerPriority getPriority() {
        return DeserializerPriority.FRAMEWORK_HIGHEST;
    }

    private AnnotatedElement valueTarget(AnnotatedElement target) {
        var valueType = valueType(target);
        if (valueType instanceof Class<?> clazz) {
            return clazz;
        }
        return new TypeTarget(valueType, target);
    }

    private Type valueType(AnnotatedElement target) {
        var parameterizedType = parameterizedType(target);
        if (parameterizedType == null) {
            return Object.class;
        }
        return parameterizedType.getActualTypeArguments()[1];
    }

    private ParameterizedType parameterizedType(AnnotatedElement target) {
        if (target instanceof TypeTarget typeTarget && typeTarget.getType() instanceof ParameterizedType parameterizedType) {
            return parameterizedType;
        }
        if (target instanceof Field field && field.getGenericType() instanceof ParameterizedType parameterizedType) {
            return parameterizedType;
        }
        if (target instanceof Parameter parameter && parameter.getParameterizedType() instanceof ParameterizedType parameterizedType) {
            return parameterizedType;
        }
        if (target instanceof RecordComponent recordComponent && recordComponent.getGenericType() instanceof ParameterizedType parameterizedType) {
            return parameterizedType;
        }
        return null;
    }

    private Map<Object, Object> createMap(Class<?> mapType) {
        if (LinkedHashMap.class.isAssignableFrom(mapType)) {
            return new LinkedHashMap<>();
        }
        if (Map.class.isAssignableFrom(mapType)) {
            return new HashMap<>();
        }
        throw new IllegalArgumentException("Unsupported map type: " + mapType);
    }

    private String path(String parent, String key) {
        return "%s/%s".formatted(parent, key);
    }
}
