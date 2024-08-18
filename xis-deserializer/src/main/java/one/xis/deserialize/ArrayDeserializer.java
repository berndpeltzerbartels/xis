package one.xis.deserialize;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import one.xis.UserContext;
import one.xis.context.XISComponent;
import one.xis.validation.Mandatory;

import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static one.xis.deserialize.DefaultDeserializationErrorType.CONVERSION_ERROR;
import static one.xis.deserialize.DefaultDeserializationErrorType.MISSING_MANDATORY_PROPERTY;

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
                                        PostProcessingObjects postProcessingObjects) throws DeserializationException, IOException {
        var list = new ArrayList<>();
        reader.beginArray();
        int index = 0;
        var componentType = getType(target).getComponentType();
        var deserializationFailed = false;
        while (reader.hasNext()) {
            var result = mainDeserializer.deserialize(reader, path(path, index), componentType, userContext, postProcessingObjects);
            if (result.isPresent()) {
                list.add(result.get());
            } else {
                list.add(null);
                deserializationFailed = true;
            }
            if (reader.peek() == JsonToken.END_ARRAY) {
                reader.endArray();
                break;
            }

            index++;
        }
        if (deserializationFailed) {
            handleDeserializationError(list, path, target, postProcessingObjects);
        }
        checkMandatory(list, target, postProcessingObjects, path);
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

    private void handleDeserializationError(List<?> values, String path, AnnotatedElement target, PostProcessingObjects postProcessingObjects) {
        var context = new ReportedErrorContext(path, target, NoAnnotation.class, UserContext.getInstance());
        postProcessingObjects.add(new InvalidValueError(context, CONVERSION_ERROR.getMessageKey(), CONVERSION_ERROR.getGlobalMessageKey()));
    }

    private void checkMandatory(List<?> values, AnnotatedElement target, PostProcessingObjects postProcessingObjects, String path) {
        if (target.isAnnotationPresent(Mandatory.class) && values.isEmpty()) {
            var context = new ReportedErrorContext(path, target, Mandatory.class, UserContext.getInstance());
            postProcessingObjects.add(new InvalidValueError(context, MISSING_MANDATORY_PROPERTY.getMessageKey(), MISSING_MANDATORY_PROPERTY.getGlobalMessageKey()));
        }
    }

    @Override
    public DeserializerPriority getPriority() {
        return DeserializerPriority.FRAMEWORK_LOW;
    }

    private String path(String parent, int index) {
        return String.format("%s[%d]", parent, index);
    }

}
