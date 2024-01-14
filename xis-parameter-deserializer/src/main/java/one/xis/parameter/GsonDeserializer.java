package one.xis.parameter;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import lombok.RequiredArgsConstructor;
import one.xis.validation.ValidatorResultElement;

import java.io.IOException;
import java.util.Optional;

@RequiredArgsConstructor
class GsonDeserializer {
    private final Gson gson;
    private final DeserializationErrorHandler deserializationErrorHandler;

    Optional<Object> read(Target target, JsonReader reader, ValidatorResultElement resultElement) throws IOException {
        var adapter = gson.getAdapter(target.getType());
        if (adapter == null) {
            deserializationErrorHandler.noAdapterFound(target, reader.nextString(), resultElement);
            return Optional.empty();
        }
        var strValue = reader.nextString();
        try {
            return Optional.of(adapter.fromJson(strValue));
        } catch (Exception e) {
            deserializationErrorHandler.conversionFailed(target, strValue, resultElement);
            return Optional.empty();
        }

    }
}