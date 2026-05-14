package one.xis.validation;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import one.xis.*;
import one.xis.context.Component;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.lang.reflect.RecordComponent;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ValidatorMessageResolver {

    private final ValidatorMessagePropertiesLoader messagePropertiesLoader;

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
        if (parameter.isAnnotationPresent(QueryParameter.class)) {
            return parameter.getAnnotation(QueryParameter.class).value();
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
        if (annotatedElement instanceof RecordComponent component) {
            return getLabelKey(component);
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

    private String getLabelKey(@NonNull RecordComponent component) {
        return component.isAnnotationPresent(LabelKey.class) ? component.getAnnotation(LabelKey.class).value() : component.getName();
    }

    private String getLabelKey(@NonNull Class<?> field) {
        return field.isAnnotationPresent(LabelKey.class) ? field.getAnnotation(LabelKey.class).value() : field.getName();
    }

    private String getMessage(String messageKey, UserContext userContext) {
        return messagePropertiesLoader.getMessage(messageKey, userContext.getLocale());
    }

}
