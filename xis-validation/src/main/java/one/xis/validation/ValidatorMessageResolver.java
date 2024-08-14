package one.xis.validation;

import lombok.NonNull;
import one.xis.*;
import one.xis.context.XISComponent;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Stream;

@XISComponent
public class ValidatorMessageResolver {

    public String createMessage(@NonNull String messageKey, @NonNull Map<String, Object> messageParameters, @NonNull AnnotatedElement annotatedElement, @NonNull UserContext userContext) {
        var message = createMessage(messageKey, messageParameters, userContext);
        if (message == null) {
            return null;
        }
        var label = getLabel(annotatedElement, userContext);
        return label == null ? message : message.replace("${label}", label);
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

    private String getLabel(@NonNull AnnotatedElement annotatedElement, @NonNull UserContext userContext) {
        if (annotatedElement instanceof Field field) {
            return getLabel(field, userContext);
        }
        if (annotatedElement instanceof Parameter parameter) {
            return getLabel(parameter, userContext);
        }
        if (annotatedElement instanceof Class<?> clazz) {
            return getLabel(clazz, userContext);
        }
        throw new IllegalArgumentException("Unsupported AnnotatedElement: " + annotatedElement);
    }

    private String getLabel(@NonNull Field field, @NonNull UserContext userContext) {
        var key = field.isAnnotationPresent(LabelKey.class) ? field.getAnnotation(LabelKey.class).messageKey() : field.getName();
        return getMessage(key, userContext);
    }

    private String getLabel(@NonNull Parameter parameter, @NonNull UserContext userContext) {
        var key = parameter.isAnnotationPresent(LabelKey.class) ? parameter.getAnnotation(LabelKey.class).messageKey() : getParameterName(parameter);
        return getMessage(key, userContext);
    }

    private String getLabel(@NonNull Class<?> field, @NonNull UserContext userContext) {
        var key = field.isAnnotationPresent(LabelKey.class) ? field.getAnnotation(LabelKey.class).messageKey() : field.getName();
        return getMessage(key, userContext);
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
