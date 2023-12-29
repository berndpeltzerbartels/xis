package one.xis.server;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
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

    @Override
    public String toString() {
        return "ValidatorResultElement{" + path + '}';
    }

    static ValidatorResultElement rootResult() {
        return new ValidatorResultElement("", 0);
    }

    ValidatorResultElement childElement(String pathElement, int index) {
        return new ValidatorResultElement(pathElement, index, this);
    }

    void setErrorIfEmpty(DefaultValidationErrorType errorType, String message, Object value) {
        if (!errors.containsKey(this.path)) {
            errors.put(this.path, new ValidationError(errorType, message, value));
        }
    }

    boolean hasError() {
        return errors.containsKey(this.path);
    }

}
