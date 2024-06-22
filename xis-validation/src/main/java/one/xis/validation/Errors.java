package one.xis.validation;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import one.xis.UserContext;
import one.xis.utils.lang.FieldUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Intended to provice a compfortable way to add errors to a {@link ValidationErrors} object.
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class Errors {

    private final Object value;
    private final ValidationErrors errors;
    private final PathElement objectPathElement;
    private final TypeValidator<?> validator;

    /**
     * Adds a field error to the {@link ValidationErrors} object.
     *
     * @param messageKey the message key of the error message in resource properties‚
     */
    public void addFieldError(String fieldName, String messageKey) {
        addFieldError(fieldName, messageKey, Map.of());
    }

    public void addFieldError(String fieldName, String messageKey, Map<String, Object> parameters) {
        var path = objectPathElement.getPath() + "/" + fieldName;
        var field = FieldUtil.getField(value.getClass(), fieldName);
        var label = ValidationUtil.getLabel(field, UserContext.getInstance());
        var parameterMap = new HashMap<>(parameters);
        parameterMap.put("label", label);
        if (!errors.hasError(path)) {
            var message = validator.createMessage(messageKey, parameterMap, UserContext.getInstance());
            errors.addError(path, message);
        }
    }

    public void addGlobalError(String messageKey) {
        addGlobalError(messageKey, Map.of());
    }

    public void addGlobalError(String messageKey, Map<String, Object> parameters) {
        var globalMessage = validator.createMessage(messageKey, parameters, UserContext.getInstance());
        errors.addGlobalError(globalMessage);
    }
}
