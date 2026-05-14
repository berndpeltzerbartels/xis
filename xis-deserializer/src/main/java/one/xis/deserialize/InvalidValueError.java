package one.xis.deserialize;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;


@EqualsAndHashCode(callSuper = true)
public class InvalidValueError extends PostProcessingResult {

    @Getter
    private final Map<String, Object> messageParameters = new HashMap<>();

    public InvalidValueError(DeserializationContext deserializationContext, String messageKey, String globalMessageKey, Object value) {
        super(deserializationContext, messageKey, globalMessageKey, value);
    }

    public InvalidValueError(DeserializationContext deserializationContext, String messageKey, String globalMessageKey, Object value, Map<String, Object> messageParameters) {
        super(deserializationContext, messageKey, globalMessageKey, value);
        this.messageParameters.putAll(messageParameters);
    }


    @Override
    public boolean reject() {
        return true;
    }
}
