package one.xis.deserialize;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import one.xis.UserContext;
import one.xis.context.Component;

import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.util.Optional;
import java.util.function.Predicate;

@Component
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
                                        PostProcessingResults results) throws IOException {
        try {
            return Optional.ofNullable(reader.nextString()).filter(Predicate.not(String::isEmpty));
        } catch (IOException e) {
            throw new DeserializationException(e, null);
        }
    }

    @Override
    public DeserializerPriority getPriority() {
        return DeserializerPriority.FRAMEWORK_LOW;
    }
}
