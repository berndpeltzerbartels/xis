package one.xis.validation;


import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
class ValidationError {
    private final DefaultValidationErrorType errorType;
    private final String message;
    private final Object value;

    public ValidationError(DefaultValidationErrorType errorType, Object value) {
        this.errorType = errorType;
        this.value = value;
        this.message = null;
    }
}
