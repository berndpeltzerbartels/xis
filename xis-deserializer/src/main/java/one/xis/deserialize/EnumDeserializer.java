package one.xis.deserialize;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import one.xis.UserContext;
import one.xis.context.Component;

import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.util.Optional;

@Component
@SuppressWarnings({"unchecked", "rawtypes"})
class EnumDeserializer implements JsonDeserializer<Enum> {
    @Override
    public boolean matches(JsonToken token, AnnotatedElement target) {
        return getType(target).isEnum();
    }

    @Override
    public Optional<Enum> deserialize(JsonReader reader, String path, AnnotatedElement target, UserContext userContext, MainDeserializer mainDeserializer, PostProcessingResults results) throws IOException {
        var input = reader.nextString();
        try {
            return Optional.of(Enum.valueOf((Class<Enum>) getType(target), input));
        } catch (Exception e) {
            if ("".equals(input)) {
                return Optional.empty();
            }
            throw new DeserializationException(e, input);
        }
    }


    @Override
    public DeserializerPriority getPriority() {
        return DeserializerPriority.FRAMEWORK_LOW;
    }
}
