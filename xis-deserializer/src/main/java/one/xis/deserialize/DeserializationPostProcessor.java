package one.xis.deserialize;

import java.util.Collection;

public interface DeserializationPostProcessor {

    void postProcess(ReportedErrorContext reportedErrorContext, Object value, Collection<ReportedError> failed);
}
