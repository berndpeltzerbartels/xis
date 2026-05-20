package one.xis.deserialize;

/**
 * Post-processing result for security checks that must stop invocation instead of becoming form validation messages.
 */
public class AccessDeniedError extends PostProcessingResult {

    private final boolean authenticationRequired;

    public AccessDeniedError(DeserializationContext deserializationContext, String message, boolean authenticationRequired) {
        super(deserializationContext, "", "", message);
        this.authenticationRequired = authenticationRequired;
    }

    @Override
    public boolean reject() {
        return false;
    }

    @Override
    public boolean authenticate() {
        return true;
    }

    public boolean isAuthenticationRequired() {
        return authenticationRequired;
    }

    public String getMessage() {
        return String.valueOf(getValue());
    }
}
