package one.xis.validation;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class ValidatorException extends Exception {

    private final Map<String, Object> messageParameters = new HashMap<>();

    public ValidatorException() {
        super();
    }

    public ValidatorException(Map<String, Object> messageParameters) {
        super();
        this.messageParameters.putAll(messageParameters);
    }

}
