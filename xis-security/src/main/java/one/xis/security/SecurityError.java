package one.xis.security;

import one.xis.deserialize.DeserializationContext;
import one.xis.deserialize.PostProcessingObject;

class SecurityError extends PostProcessingObject {

    public SecurityError(DeserializationContext deserializationContext, String messageKey, String globalMessageKey) {
        super(deserializationContext, messageKey, globalMessageKey);
    }

    @Override
    public boolean reject() {
        return true;
    }
}
