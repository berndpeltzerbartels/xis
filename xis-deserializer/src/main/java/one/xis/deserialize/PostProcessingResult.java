package one.xis.deserialize;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@RequiredArgsConstructor
public abstract class PostProcessingResult {

    private final DeserializationContext deserializationContext;
    private final String messageKey;
    private final String globalMessageKey;

    private final Map<String, Object> messageParameters = new HashMap<>();

    /**
     * Allows adding parameters for validator messages, like
     * "Do not forget to set the value of ${variableName}"
     *
     * @param variableName the name of the variable in the message, like "variableName" in the example above
     * @param messageKey   the key for the message in resource bundle
     * @param messageKey   the key for the message in resource bundle
     */
    public void addMessageParameter(String variableName, Object messageKey) {
        messageParameters.put(variableName, messageKey);
    }


    /**
     * @return true if processing the current request should be stopped
     */
    public abstract boolean reject();

    public boolean authenticate() {
        return false;
    }
}
