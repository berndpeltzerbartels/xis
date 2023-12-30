package one.xis.validation;

import one.xis.context.XISComponent;
import one.xis.context.XISInject;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

@XISComponent
public class ValidationImpl implements Validation {

    @XISInject
    @SuppressWarnings("rawtypes")
    private Collection<Validator> validators;

    @XISInject
    private ValidatorMessageResolver messageResolver;


    @Override
    public void assignmentError(Class<?> valueType, Object value, ValidatorResultElement validatorResultElement) {
        var type = Arrays.stream(DefaultValidationErrorType.values())
                .filter(errorType -> matchingFieldErrorType(errorType, valueType))
                .findFirst().orElseThrow(() -> new IllegalStateException("unmatched validation error for " + valueType));
        var message = messageResolver.resolveMessage(type, value);
        validatorResultElement.setErrorIfEmpty(type, message, value);
    }

    @Override
    public void assignmentError(Class<?> valueType, ValidatorResultElement validatorResultElement) {

    }

    @Override
    public boolean validateBeforeAssignment(Class<?> valueType, Object value, ValidatorResultElement validatorResultElement) {

        return true;
    }


    @Override
    public void validateAssignedValue(Class<?> valueType, Object value, ValidatorResultElement validatorResultElement) {

    }


    private static boolean matchingFieldErrorType(DefaultValidationErrorType errorType, Class<?> valueType) {
        return Arrays.stream(DefaultValidationErrorType.values()).map(DefaultValidationErrorType::getFieldTypes)
                .flatMap(Set::stream)
                .anyMatch(type -> type.isAssignableFrom(valueType));
    }


}
