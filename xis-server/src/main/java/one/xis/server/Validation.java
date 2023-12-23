package one.xis.server;

import one.xis.context.XISComponent;
import one.xis.context.XISInject;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

@XISComponent
class Validation {

    @XISInject
    private Collection<Validator<?>> validators;

    @XISInject
    private ValidatorMessageResolver messageResolver;


    void assignmentError(JsonDeserializer.Target target, Object value, ValidatorResultElement validatorResultElement) {
        var type = Arrays.stream(DefaultValidationErrorType.values())
                .filter(errorType -> matchingFieldErrorType(target, errorType))
                .findFirst().orElseThrow(() -> new IllegalStateException("unmatched validation error for " + target));
        var message = messageResolver.resolveMessage(type, target, value);
        validatorResultElement.setErrorIfEmpty(type, message, value);
    }

    boolean validateBeforeAssignment(JsonDeserializer.Target target, Object value, ValidatorResultElement validatorResultElement) {

        return true;
    }


    void validateAssignedValue(JsonDeserializer.Target target, Object value, ValidatorResultElement validatorResultElement) {

    }


    private static boolean matchingFieldErrorType(JsonDeserializer.Target target, DefaultValidationErrorType errorType) {
        return Arrays.stream(DefaultValidationErrorType.values()).map(DefaultValidationErrorType::getFieldTypes)
                .flatMap(Set::stream)
                .anyMatch(type -> type.isAssignableFrom(target.getType()));
    }


}
