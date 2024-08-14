package one.xis.deserialize;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class ReportedError {
    private final ReportedErrorContext reportedErrorContext;
    private final String messageKey;
    private final String globalMessageKey;
    private final Map<String, Object> messageParameters = new HashMap<>();

    public void addMessageParameter(String key, Object value) {
        messageParameters.put(key, value);
    }
}
