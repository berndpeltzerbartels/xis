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
class BooleanDeserializer implements JsonDeserializer<Boolean> {

    @Override
    public boolean matches(JsonToken token, AnnotatedElement target) {
        var targetType = getType(target);
        return targetType.equals(Boolean.class) || targetType.equals(boolean.class);
    }

    @Override
    public Optional<Boolean> deserialize(JsonReader reader,
                                         String path,
                                         AnnotatedElement target,
                                         UserContext userContext,
                                         MainDeserializer mainDeserializer,
                                         PostProcessingResults results) throws IOException {
        try {
            if (reader.peek().equals(JsonToken.BOOLEAN)) {
                return Optional.of(reader.nextBoolean());
            }
            if (reader.peek().equals(JsonToken.STRING)) {
                String value = reader.nextString();
                return switch (value) {
                    case "true", "1" -> Optional.of(true);
                    case "false", "0" -> Optional.of(false);
                    default -> {
                        if (value.isEmpty()) {
                            yield Optional.empty();
                        } else {
                            throw new DeserializationException("Invalid boolean value: " + value, value);
                        }
                    }
                };
            }
            if (reader.peek().equals(JsonToken.NULL)) {
                reader.nextNull();
                return Optional.of(false);
            }
            reader.skipValue();
            throw new DeserializationException("Expected a boolean, string, or null for boolean deserialization, but found: ", reader.peek());
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
