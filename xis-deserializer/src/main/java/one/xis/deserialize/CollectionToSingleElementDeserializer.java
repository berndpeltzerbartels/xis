package one.xis.deserialize;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import one.xis.Format;
import one.xis.UserContext;
import one.xis.context.XISComponent;
import one.xis.validation.Mandatory;

import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.util.Optional;

import static one.xis.deserialize.DefaultDeserializationErrorType.MISSING_MANDATORY_PROPERTY;

@XISComponent
class CollectionToSingleElementDeserializer implements JsonDeserializer<Object> {

    @Override
    public DeserializerPriority getPriority() {
        return DeserializerPriority.FRAMEWORK_HIGHEST;
    }

    @Override
    public boolean matches(JsonToken token, AnnotatedElement target) {
        return token == JsonToken.BEGIN_ARRAY && !Iterable.class.isAssignableFrom(getType(target)) && !getType(target).isArray();
    }

    @Override
    public Optional<Object> deserialize(JsonReader reader, String path, AnnotatedElement target, UserContext userContext, MainDeserializer mainDeserializer, PostProcessingResults results) throws DeserializationException, IOException {
        reader.beginArray();
        var componentType = getType(target);
        Object value = null;
        if (reader.hasNext()) {
            Optional<Object> result = Optional.empty();
            if (target.isAnnotationPresent(Format.class)) {
                result = mainDeserializer.getDeserializer(FormattedDeserializer.class).deserialize(reader, path, target, userContext, mainDeserializer, results);
            } else {
                result = mainDeserializer.deserialize(reader, path, componentType, userContext, results).map(Object.class::cast);
            }
            if (result.isPresent()) {
                value = result.get();
            } else if (target.isAnnotationPresent(Mandatory.class)) {
                var context = new DeserializationContext(path, target, Mandatory.class, userContext);
                results.add(new InvalidValueError(context, MISSING_MANDATORY_PROPERTY.getMessageKey(), MISSING_MANDATORY_PROPERTY.getGlobalMessageKey()));
            }
            if (reader.peek() == JsonToken.END_ARRAY) {
                reader.endArray();
            } else {
                throw new DeserializationException("Expected single element collection, but got multiple elements");
            }
        }
        return Optional.ofNullable(value);

    }


    private String path(String parent, int index) {
        return String.format("%s[%d]", parent, index);
    }

}
