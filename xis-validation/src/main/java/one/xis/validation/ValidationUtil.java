package one.xis.validation;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import one.xis.*;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Stream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ValidationUtil {

    static String createMessage(@NonNull String messageKey, @NonNull Map<String, Object> parameters, @NonNull Parameter parameter, @NonNull UserContext userContext) {
        var message = createMessage(messageKey, parameters, userContext);
        if (message == null) {
            return null;
        }
        var label = getLabel(parameter, userContext);
        return label == null ? message : message.replace("${label}", label);
    }

    static String createMessage(@NonNull String messageKey, @NonNull Map<String, Object> parameters, @NonNull Field field, @NonNull UserContext userContext) {
        var message = createMessage(messageKey, parameters, userContext);
        if (message == null) {
            return null;
        }
        var label = getLabel(field, userContext);
        return label == null ? message : message.replace("${label}", label);
    }


    static String createMessage(@NonNull String messageKey, @NonNull Map<String, Object> parameters, @NonNull UserContext userContext) {
        var message = getMessage(messageKey, userContext);
        if (message == null) {
            return null;
        }
        for (var key : parameters.keySet()) {
            var valueStr = parameters.get(key);
            var value = valueStr == null ? "null" : parseParameter(valueStr.toString(), userContext);
            message = message.replace("${" + key + "}", value);
        }
        return message;
    }


    private static String parseParameter(@NonNull String parameter, @NonNull UserContext userContext) {
        int beginVar = parameter.indexOf("${");
        while (beginVar != -1) {
            int endVar = parameter.indexOf("}", beginVar);
            if (endVar == -1) {
                break;
            }
            String var = parameter.substring(beginVar, endVar + 1);
            String key = var.substring(2, var.length() - 1);
            String value = getMessage(key, userContext);
            if (value != null) {
                parameter = parameter.replace(var, value);
            }
            beginVar = parameter.indexOf("${", endVar);
        }
        return parameter;
    }

    public static String getParameterName(Parameter parameter) {
        if (parameter.isAnnotationPresent(FormData.class)) {
            return parameter.getAnnotation(FormData.class).value();
        }
        if (parameter.isAnnotationPresent(ModelData.class)) {
            return parameter.getAnnotation(ModelData.class).value();
        }
        if (parameter.isNamePresent()) {
            return parameter.getName();
        }
        if (parameter.isAnnotationPresent(PathVariable.class)) {
            return parameter.getAnnotation(PathVariable.class).value();
        }
        if (parameter.isAnnotationPresent(URLParameter.class)) {
            return parameter.getAnnotation(URLParameter.class).value();
        }
        if (parameter.isAnnotationPresent(UserId.class)) {
            return "userId";
        }
        if (parameter.isAnnotationPresent(ClientId.class)) {
            return "clientId";
        }

        return parameter.getName();
    }

    public static String getLabel(@NonNull Field field, @NonNull UserContext userContext) {
        var key = field.isAnnotationPresent(LabelKey.class) ? field.getAnnotation(LabelKey.class).messageKey() : field.getName();
        return getMessage(key, userContext);
    }

    public static String getLabel(@NonNull Parameter parameter, @NonNull UserContext userContext) {
        var key = parameter.isAnnotationPresent(LabelKey.class) ? parameter.getAnnotation(LabelKey.class).messageKey() : ValidationUtil.getParameterName(parameter);
        return getMessage(key, userContext);
    }


    private static String getMessage(String messageKey, UserContext userContext) {
        return Stream.of(getCustomValidationResourceBundle(userContext), getMessagesResourceBundle(userContext), getDefaultValidationResourceBundle(userContext))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(bundle -> getString(messageKey, bundle))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .orElse(null);

    }

    private static Optional<ResourceBundle> getCustomValidationResourceBundle(UserContext userContext) {
        try {
            return Optional.of(ResourceBundle.getBundle("validation.messages", userContext.getLocale()));
        } catch (MissingResourceException e) {
            return Optional.empty();
        }
    }

    private static Optional<ResourceBundle> getDefaultValidationResourceBundle(UserContext userContext) {
        try {
            return Optional.of(ResourceBundle.getBundle("validation.default-messages", userContext.getLocale()));
        } catch (MissingResourceException e) {
            return Optional.empty();
        }
    }

    private static Optional<ResourceBundle> getMessagesResourceBundle(UserContext userContext) {
        try {
            return Optional.of(ResourceBundle.getBundle("messages", userContext.getLocale()));
        } catch (MissingResourceException e) {
            return Optional.empty();
        }
    }

    private static Optional<String> getString(String messageKey, ResourceBundle resourceBundle) {
        try {
            return Optional.of(resourceBundle.getString(messageKey));
        } catch (MissingResourceException e) {
            return Optional.empty();
        }
    }
}
