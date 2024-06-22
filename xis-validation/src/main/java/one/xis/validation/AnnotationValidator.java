package one.xis.validation;

import one.xis.UserContext;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.Map;

/**
 * Validator for annotation based validation. Working for parameters and fields.
 */
public interface AnnotationValidator {

    int DEFAULT_PRIORITY = 0;
    int FRAMEWORK_PRIORITY = 1000;
    int HIGHEST_PRIORITY = Integer.MAX_VALUE;
    int LOWEST_PRIORITY = Integer.MIN_VALUE;

    void validate(AnnotatedElement annotatedElement, Object value) throws ValidatorException;


    /**
     * Create a message for a field. Override this method to provide a custom message
     * not present in the message bundle.
     *
     * @param messageKey
     * @param parameters
     * @param field
     * @param userContext
     * @return
     */
    default String createMessage(String messageKey, Map<String, Object> parameters, Field field, UserContext userContext) {
        return ValidationUtil.createMessage(messageKey, parameters, field, userContext);
    }

    /**
     * Create a global message for a parameter. Override this method to provide a custom message
     * not present in the message bundle.
     *
     * @param messageKey
     * @param parameters
     * @param field
     * @param userContext
     * @return
     */
    default String createGlobalMessage(String messageKey, Map<String, Object> parameters, Field field, UserContext userContext) {
        return ValidationUtil.createMessage(messageKey, parameters, field, userContext);
    }


    /**
     * Create a message for a parameter. Override this method to provide a custom message
     * not present in the message bundle.
     *
     * @param messageKey
     * @param parameters
     * @param parameter
     * @param userContext
     * @return
     */
    default String createMessage(String messageKey, Map<String, Object> parameters, Parameter parameter, UserContext userContext) {
        return ValidationUtil.createMessage(messageKey, parameters, parameter, userContext);
    }

    /**
     * Create a global message for a parameter. Override this method to provide a custom message
     * not present in the message bundle.
     *
     * @param messageKey
     * @param parameters
     * @param parameter
     * @param userContext
     * @return
     */
    default String createGlobalMessage(String messageKey, Map<String, Object> parameters, Parameter parameter, UserContext userContext) {
        return ValidationUtil.createMessage(messageKey, parameters, parameter, userContext);
    }


    default int priority() {
        return DEFAULT_PRIORITY;
    }


}
