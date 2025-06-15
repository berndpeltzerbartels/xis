package one.xis.deserialize;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import one.xis.UserContext;
import one.xis.context.XISComponent;
import one.xis.validation.AllElementsMandatory;

import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@XISComponent
class ArrayDeserializer implements JsonDeserializer<Object> {

    @Override
    public boolean matches(JsonToken token, AnnotatedElement target) {
        return getType(target).isArray();
    }

    @Override
    public Optional<Object> deserialize(JsonReader reader,
                                        String path,
                                        AnnotatedElement target,
                                        UserContext userContext,
                                        MainDeserializer mainDeserializer,
                                        PostProcessingResults postProcessingResults) throws DeserializationException, IOException {
        var list = new ArrayList<>();
        reader.beginArray();
        int index = 0;
        var componentType = getType(target).getComponentType();
        var deserializationFailed = false;
        while (reader.hasNext()) {
            Optional<Object> result = Optional.empty();
            if (this.requiresFormatter(target)) {
                result = mainDeserializer.getDeserializer(FormattedDeserializer.class).deserialize(reader, path(path, index), target, userContext, mainDeserializer, postProcessingResults);
            } else {
                result = mainDeserializer.deserialize(reader, path(path, index), componentType, userContext, postProcessingResults).map(Object.class::cast);
            }
            if (result.isPresent()) {
                list.add(result.get());
            } else {
                list.add(null);
                if (componentType.isPrimitive() || target.isAnnotationPresent(AllElementsMandatory.class)) {
                    deserializationFailed = true;
                }
            }
            if (reader.peek() == JsonToken.END_ARRAY) {
                reader.endArray();
                break;
            }

            index++;
        }
        if (deserializationFailed) {
            handleDeserializationError(list, path, target, postProcessingResults, userContext);
        }
        checkMandatory(list, target, postProcessingResults, path, userContext);
        return Optional.of(toArray(list, componentType));
    }

    private Object toArray(List<?> list, Class<?> componentType) {
        var arr = Array.newInstance(componentType, list.size());
        for (int i = 0; i < list.size(); i++) {
            var value = list.get(i);
            if (value == null) {
                continue; // Otherwise, array of primitives will throw an IllegalArgumentException
            }
            Array.set(arr, i, value);
        }
        return arr;
    }

    @Override
    public DeserializerPriority getPriority() {
        return DeserializerPriority.FRAMEWORK_LOW;
    }

    private String path(String parent, int index) {
        return String.format("%s[%d]", parent, index);
    }

}
