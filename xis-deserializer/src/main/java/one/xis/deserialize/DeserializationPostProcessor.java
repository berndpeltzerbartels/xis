package one.xis.deserialize;

import one.xis.ImportInstances;

@ImportInstances
public interface DeserializationPostProcessor {

    void postProcess(DeserializationContext deserializationContext, Object value, PostProcessingResults results);
}
