package one.xis.parameter;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;

import java.io.IOException;
import java.io.StringReader;
import java.util.Optional;

@XISComponent
@RequiredArgsConstructor
class DefaultJsonDeserializer implements JsonDeserializer<Object> {

    private final Gson gson;

    @Override
    public boolean matchesTarget(Target target, JsonToken token) {
        return true;
    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public Optional<Object> deserialize(@NonNull String value, Target target, ParameterDeserializationContext context) throws IOException, ConversionException {
        try {
            if (target.getType().equals(String.class)) {
                return Optional.of(value);
            }
            var reader = new JsonReader(new StringReader(value));
            reader.setLenient(true);
            var adapter = gson.getAdapter(target.getType());
            return Optional.of(adapter.read(reader));
        } catch (Exception e) {
            context.getValidatorResultElement().setErrorIfEmpty(value);
            return Optional.empty();
        }
    }
}
