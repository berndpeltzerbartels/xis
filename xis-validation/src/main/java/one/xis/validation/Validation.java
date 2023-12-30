package one.xis.validation;

public interface Validation {
    void assignmentError(Class<?> valueType, Object value, ValidatorResultElement validatorResultElement);

    void assignmentError(Class<?> valueType, ValidatorResultElement validatorResultElement);

    boolean validateBeforeAssignment(Class<?> valueType, Object value, ValidatorResultElement validatorResultElement);

    void validateAssignedValue(Class<?> valueType, Object value, ValidatorResultElement validatorResultElement);
}
