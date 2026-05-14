package one.xis.deserialize;

import lombok.NonNull;
import one.xis.ImportInstances;

@ImportInstances
public interface DeserializationPostProcessor {

    void postProcess(@NonNull DeserializationContext deserializationContext, @NonNull Object value, @NonNull PostProcessingResults results);
}
