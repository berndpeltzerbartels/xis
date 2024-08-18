package one.xis.deserialize;

public interface DeserializationPostProcessor {

    void postProcess(DeserializationContext deserializationContext, Object value, PostProcessingResults results);
}
