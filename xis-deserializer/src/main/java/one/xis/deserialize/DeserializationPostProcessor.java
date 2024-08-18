package one.xis.deserialize;

public interface DeserializationPostProcessor {

    void postProcess(ReportedErrorContext reportedErrorContext, Object value, PostProcessingObjects results);
}
