package one.xis.validation;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Checked exception thrown by {@link Validator} implementations when a value is
 * invalid.
 *
 * <p>The optional message parameters are passed to the validation message
 * resolver and can be used in localized message templates.</p>
 */
@Getter
public class ValidatorException extends Exception {

    private final Map<String, Object> messageParameters = new HashMap<>();

    public ValidatorException() {
        super();
    }

    /**
     * Creates a validation failure with parameters for message interpolation.
     */
    public ValidatorException(Map<String, Object> messageParameters) {
        super();
        this.messageParameters.putAll(messageParameters);
    }

}
