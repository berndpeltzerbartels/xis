package one.xis.validation;

import one.xis.UserContext;
import one.xis.utils.lang.FieldUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.Map;

/**
 * Validator fo complex type. Intention is to validate multiple fields of a complex type in a single
 * validation step. This can be helpful to validate a complex object with multiple fields dependent
 * on each other.
 *
 * @param <T> the type to validate
 */
public interface TypeValidator<T> {

    int DEFAULT_PRIORITY = 0;
    int FRAMEWORK_PRIORITY = 1000;
    int HIGHEST_PRIORITY = Integer.MAX_VALUE;
    int LOWEST_PRIORITY = Integer.MIN_VALUE;

    void validate(T value, Errors errors) throws ValidationFailedException;


    @SuppressWarnings("unused")
    default String createMessage(String messageKey, Map<String, Object> parameters, UserContext userContext) {
        return ValidationUtil.createMessage(messageKey, parameters, userContext);
    }


    default String createMessage(String messageKey, Map<String, Object> parameters, Parameter parameter, UserContext userContext) {
        return ValidationUtil.createMessage(messageKey, parameters, parameter, userContext);
    }


    default int priority() {
        return DEFAULT_PRIORITY;
    }

    default Field field(Object o, String fieldName) {
        return FieldUtil.getField(o.getClass(), fieldName);
    }

}
