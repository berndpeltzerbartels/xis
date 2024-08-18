package one.xis.deserialize;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import one.xis.UserContext;
import one.xis.context.XISComponent;

import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.util.Optional;

@XISComponent
class StringDeserializer implements JsonDeserializer<String> {
    @Override
    public boolean matches(JsonToken token, AnnotatedElement target) {
        return String.class.isAssignableFrom(getType(target));
    }

    @Override
    public Optional<String> deserialize(JsonReader reader,
                                        String path,
                                        AnnotatedElement target,
                                        UserContext userContext,
                                        MainDeserializer mainDeserializer,
                                        PostProcessingObjects results) throws IOException {
        try {
            return Optional.of(reader.nextString());
        } catch (IOException e) {
            throw new DeserializationException(e);
        }
    }

    @Override
    public DeserializerPriority getPriority() {
        return DeserializerPriority.FRAMEWORK_LOW;
    }
}
