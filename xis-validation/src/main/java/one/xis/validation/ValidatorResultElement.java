package one.xis.validation;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public
class ValidatorResultElement {
    private final String path;
    private final int index;
    private final ValidatorResultElement parent;
    private final Map<String, ValidationError> errors;

    ValidatorResultElement(String path, int index, ValidatorResultElement parent) {
        this.path = String.format("%s/%s[%d]", parent.getPath(), path, index);
        this.index = index;
        this.parent = parent;
        this.errors = parent.getErrors();
    }

    private ValidatorResultElement(String path, int index) {
        this.path = path;
        this.index = index;
        this.parent = null;
        this.errors = new HashMap<>();
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    @Override
    public String toString() {
        return "ValidatorResultElement{" + path + '}';
    }

    public static ValidatorResultElement rootResult() {
        return new ValidatorResultElement("", 0);
    }

    public ValidatorResultElement childElement(String pathElement, int index) {
        return new ValidatorResultElement(pathElement, index, this);
    }

    public void setErrorIfEmpty(DefaultValidationErrorType errorType, String message, Class<?> targetType, Object value) {
        if (!errors.containsKey(this.path)) {
            errors.put(this.path, new ValidationError(errorType, message, value));
        }
    }

    public void setErrorIfEmpty(Class<?> targetType) {
        if (!errors.containsKey(this.path)) {
            errors.put(this.path, new ValidationError(errorType(targetType), null, null));
        }
    }

    public void setErrorIfEmpty(Object value, Class<?> type) {
        if (!errors.containsKey(this.path)) {
            errors.put(this.path, new ValidationError(errorType(type), value));
        }
    }

    private DefaultValidationErrorType errorType(Class<?> targetType) {
        return DefaultValidationErrorType.errorForType(targetType).orElse(DefaultValidationErrorType.ILLEGAL_UNKNOWN_REASON);
    }

    public boolean hasError() {
        return errors.containsKey(this.path);
    }

}
