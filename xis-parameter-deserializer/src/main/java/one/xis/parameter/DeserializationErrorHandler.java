package one.xis.parameter;

import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.validation.DefaultValidationErrorType;
import one.xis.validation.ValidatorMessageResolver;
import one.xis.validation.ValidatorResultElement;

@XISComponent
@RequiredArgsConstructor
class DeserializationErrorHandler {
    private final ValidatorMessageResolver validatorMessageResolver;

    void noAdapterFound(Target target, String value, ValidatorResultElement resultElement) {
        var errorType = DefaultValidationErrorType.NO_TYPE_ADAPTER;
        var message = validatorMessageResolver.resolveMessage(errorType, target.getType(), value);
        resultElement.setErrorIfEmpty(DefaultValidationErrorType.NO_TYPE_ADAPTER, message, target.getType(), value);
    }

    void conversionFailed(Target target, String value, ValidatorResultElement resultElement) {
        var errorType = DefaultValidationErrorType.errorForType(target.getType()).orElseThrow(() -> new RuntimeException("no error type for " + target.getType()));
        var message = validatorMessageResolver.resolveMessage(errorType, target.getType(), value);
        resultElement.setErrorIfEmpty(errorType, message, target.getType(), value);
    }

    void injectionFailed(Target target, String value, ValidatorResultElement resultElement) {
        var errorType = DefaultValidationErrorType.errorForType(target.getType()).orElseThrow(() -> new RuntimeException("no error type for " + target.getType()));
        var message = validatorMessageResolver.resolveMessage(errorType, target.getType(), value);
        resultElement.setErrorIfEmpty(errorType, message, target.getType(), value);
    }
}
