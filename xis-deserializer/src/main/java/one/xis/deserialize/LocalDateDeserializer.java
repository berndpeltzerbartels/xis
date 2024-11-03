package one.xis.deserialize;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import one.xis.UserContext;
import one.xis.context.XISComponent;

import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.time.LocalDate;
import java.util.Optional;

@XISComponent
class LocalDateDeserializer implements JsonDeserializer<LocalDate> { // TODO: Implement JsonDeserializer for all date types
    @Override
    public boolean matches(JsonToken token, AnnotatedElement target) {
        return LocalDate.class.isAssignableFrom(getType(target));
    }

    @Override
    public Optional<LocalDate> deserialize(JsonReader reader,
                                           String path,
                                           AnnotatedElement target,
                                           UserContext userContext,
                                           MainDeserializer mainDeserializer,
                                           PostProcessingResults results) throws IOException {
        var value = reader.nextString();
        try {
            return Optional.of(LocalDate.parse(value));
        } catch (Exception e) {
            throw new DeserializationException(e, value);
        }

    }

    @Override
    public DeserializerPriority getPriority() {
        return DeserializerPriority.FRAMEWORK_LOW;
    }
}
