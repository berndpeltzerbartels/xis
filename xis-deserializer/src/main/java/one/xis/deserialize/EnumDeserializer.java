package one.xis.deserialize;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import one.xis.UserContext;
import one.xis.context.XISComponent;

import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.util.Optional;

@XISComponent
@SuppressWarnings({"unchecked", "rawtypes"})
class EnumDeserializer implements JsonDeserializer<Enum> {
    @Override
    public boolean matches(JsonToken token, AnnotatedElement target) {
        return getType(target).isEnum();
    }

    @Override
    public Optional<Enum> deserialize(JsonReader reader, String path, AnnotatedElement target, UserContext userContext, MainDeserializer mainDeserializer, PostProcessingObjects results) throws IOException {
        try {
            return Optional.of(Enum.valueOf((Class<Enum>) getType(target), reader.nextString()));
        } catch (Exception e) {
            throw new DeserializationException(e);
        }
    }


    @Override
    public DeserializerPriority getPriority() {
        return DeserializerPriority.FRAMEWORK_LOW;
    }
}
