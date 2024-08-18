package one.xis.security;

import one.xis.deserialize.DeserializationContext;
import one.xis.deserialize.PostProcessingResult;

class SecurityError extends PostProcessingResult {

    public SecurityError(DeserializationContext deserializationContext, String messageKey, String globalMessageKey) {
        super(deserializationContext, messageKey, globalMessageKey);
    }

    @Override
    public boolean reject() {
        return true;
    }
}
