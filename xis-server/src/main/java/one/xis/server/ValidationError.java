package one.xis.server;


import lombok.Data;

@Data
class ValidationError {
    private final DefaultValidationErrorType errorType;
    private final String message;
    private final Object value;
}
