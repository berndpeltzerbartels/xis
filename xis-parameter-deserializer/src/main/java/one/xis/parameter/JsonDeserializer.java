package one.xis.parameter;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import lombok.NonNull;

import java.io.IOException;
import java.util.Optional;

public interface JsonDeserializer<T> {


    boolean matchesTarget(Target target, JsonToken token);

    default int getPriority() {
        return 10;
    }


    default Optional<T> deserialize(JsonReader reader, Target target, ParameterDeserializationContext context) throws IOException, ConversionException {
        var value = reader.nextString();
        if (value == null) {
            return null;
        }
        return deserialize(value, target, context);
    }


    Optional<T> deserialize(@NonNull String value, Target target, ParameterDeserializationContext context) throws IOException, ConversionException;

}
