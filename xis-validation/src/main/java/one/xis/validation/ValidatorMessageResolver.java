package one.xis.validation;

import lombok.NonNull;
import one.xis.*;
import one.xis.context.XISComponent;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Stream;

@XISComponent
public class ValidatorMessageResolver {

    public String createMessage(@NonNull String messageKey, @NonNull Map<String, Object> messageParameters, @NonNull AnnotatedElement annotatedElement, @NonNull UserContext userContext) {
        var labelKey = getLabelKey(annotatedElement);
        var label = getMessage(labelKey, userContext);
        var parameters = new HashMap<>(messageParameters);
        parameters.put("label", label == null ? labelKey : label);
        var message = createMessage(messageKey, parameters, userContext);
        if (message == null) {
            return "[" + messageKey + "]";
        }
        return message;
    }


    private String createMessage(@NonNull String messageKey, @NonNull Map<String, Object> messageParameters, @NonNull UserContext userContext) {
        var message = getMessage(messageKey, userContext);
        if (message == null) {
            return null;
        }
        for (var key : messageParameters.keySet()) {
            var valueStr = messageParameters.get(key);
            var value = valueStr == null ? "null" : parseParameter(valueStr.toString(), userContext);
            message = message.replace("${" + key + "}", value);
        }
        return message;
    }


    private String parseParameter(@NonNull String parameter, @NonNull UserContext userContext) {
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

    public String getParameterName(Parameter parameter) {
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

    private String getLabelKey(@NonNull AnnotatedElement annotatedElement) {
        if (annotatedElement instanceof Field field) {
            return getLabelKey(field);
        }
        if (annotatedElement instanceof Parameter parameter) {
            return getLabelKey(parameter);
        }
        if (annotatedElement instanceof Class<?> clazz) {
            return getLabelKey(clazz);
        }
        throw new IllegalArgumentException("Unsupported AnnotatedElement: " + annotatedElement);
    }

    private String getLabelKey(@NonNull Field field) {
        return field.isAnnotationPresent(LabelKey.class) ? field.getAnnotation(LabelKey.class).value() : field.getName();
    }

    private String getLabelKey(@NonNull Parameter parameter) {
        return parameter.isAnnotationPresent(LabelKey.class) ? parameter.getAnnotation(LabelKey.class).value() : getParameterName(parameter);
    }

    private String getLabelKey(@NonNull Class<?> field) {
        return field.isAnnotationPresent(LabelKey.class) ? field.getAnnotation(LabelKey.class).value() : field.getName();
    }


    private String getMessage(String messageKey, UserContext userContext) {
        return Stream.of(getCustomValidationResourceBundle(userContext), getMessagesResourceBundle(userContext), getDefaultValidationResourceBundle(userContext))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(bundle -> getString(messageKey, bundle))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .orElse(null);

    }

    private Optional<ResourceBundle> getCustomValidationResourceBundle(UserContext userContext) {
        try {
            return Optional.of(ResourceBundle.getBundle("validation.messages", userContext.getLocale()));
        } catch (MissingResourceException e) {
            return Optional.empty();
        }
    }

    private Optional<ResourceBundle> getDefaultValidationResourceBundle(UserContext userContext) {
        try {
            return Optional.of(ResourceBundle.getBundle("validation.default-messages", userContext.getLocale()));
        } catch (MissingResourceException e) {
            return Optional.empty();
        }
    }

    private Optional<ResourceBundle> getMessagesResourceBundle(UserContext userContext) {
        try {
            return Optional.of(ResourceBundle.getBundle("messages", userContext.getLocale()));
        } catch (MissingResourceException e) {
            return Optional.empty();
        }
    }

    private Optional<String> getString(String messageKey, ResourceBundle resourceBundle) {
        try {
            return Optional.of(resourceBundle.getString(messageKey));
        } catch (MissingResourceException e) {
            return Optional.empty();
        }
    }
}
