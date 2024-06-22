package one.xis.validation;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collections;
import java.util.Map;

/**
 * Exception that is thrown when a annotation based validation fails.
 *
 * @see AnnotationValidator
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ValidatorException extends Exception {
    private final String messageKey;
    private final String globalMessageKey;
    private final Map<String, Object> parameters;

    /**
     * Create a new exception with a message key and parameters.
     *
     * @param messageKey The key of the message for lookup in the message bundle.
     * @param parameters a map of parameters for the message will get parsed into the messages
     *                   as $-variables like ${variableName}
     */
    public ValidatorException(String messageKey, Map<String, Object> parameters) {
        this.messageKey = messageKey;
        this.globalMessageKey = null;
        this.parameters = parameters;
    }

    /**
     * Create a new exception with a message key and no parameters.
     *
     * @param messageKey The key of the message for lookup in the message bundle.
     */
    public ValidatorException(String messageKey) {
        this.messageKey = messageKey;
        this.globalMessageKey = null;
        this.parameters = Collections.emptyMap();
    }


    /**
     * Create a new exception with a message key and no parameters.
     *
     * @param messageKey The key of the message for lookup in the message bundle.
     */
    public ValidatorException(String messageKey, String globalMessageKey) {
        this.messageKey = messageKey;
        this.globalMessageKey = globalMessageKey;
        this.parameters = Collections.emptyMap();
    }

}
