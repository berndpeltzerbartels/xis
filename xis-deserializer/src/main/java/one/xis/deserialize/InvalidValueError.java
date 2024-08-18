package one.xis.deserialize;

import lombok.EqualsAndHashCode;


@EqualsAndHashCode(callSuper = false)
public class InvalidValueError extends PostProcessingResult {


    public InvalidValueError(DeserializationContext deserializationContext, String messageKey, String globalMessageKey) {
        super(deserializationContext, messageKey, globalMessageKey);
    }

    @Override
    public boolean reject() {
        return true;
    }
}
