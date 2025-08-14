package one.xis.deserialize;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import lombok.RequiredArgsConstructor;
import one.xis.UserContext;
import one.xis.context.XISComponent;

import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.util.Optional;

@XISComponent
@RequiredArgsConstructor
class CharacterDeserializer implements JsonDeserializer<Character> {

    @Override
    public boolean matches(JsonToken token, AnnotatedElement target) {
        var targetType = getType(target);
        return targetType.equals(Character.class) || targetType.equals(char.class);
    }

    @Override
    public Optional<Character> deserialize(JsonReader reader,
                                           String path,
                                           AnnotatedElement target,
                                           UserContext userContext,
                                           MainDeserializer mainDeserializer,
                                           PostProcessingResults results) throws IOException {
        try {
            if (reader.peek().equals(JsonToken.STRING)) {
                String value = reader.nextString();
                if (value != null && value.length() == 1) {
                    return Optional.of(value.charAt(0));
                }
                throw new DeserializationException("Invalid character value: " + value, value);
            } else if (reader.peek().equals(JsonToken.NULL)) {
                reader.nextNull();
                throw new DeserializationException("Null value encountered for character deserialization, expected a single character string.", null);
            } else {
                reader.skipValue();
                throw new DeserializationException("Expected a string or null for character deserialization, but found: ", reader.peek());
            }
        } catch (DeserializationException e) {
            throw e; // Re-throw custom exception
        } catch (Exception e) {
            throw new DeserializationException(e, "unknown");
        }

    }

    @Override
    public DeserializerPriority getPriority() {
        return DeserializerPriority.FRAMEWORK_LOW;
    }
}