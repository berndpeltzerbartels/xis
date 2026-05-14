package one.xis.server;

import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import one.xis.*;
import one.xis.PathVariable;
import one.xis.auth.AuthenticationException;
import one.xis.deserialize.MainDeserializer;
import one.xis.deserialize.PostProcessingResults;
import one.xis.http.HttpRequest;
import one.xis.http.HttpResponse;
import one.xis.http.RequestContext;
import one.xis.utils.lang.ClassUtils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.function.Supplier;

@RequiredArgsConstructor
class ControllerMethodParameter {
    private final Method method;
    private final Parameter parameter;
    private final MainDeserializer deserializer;
    private final int parameterIndex;
    private final int positionalParameterIndex;

    // TODO Validation: only one of these annotation in parameter
    Object prepareParameter(ClientRequest request, PostProcessingResults postProcessingResults, Map<String, Object> requestScope) throws Exception {
        var userContext = UserContextImpl.getInstance();
        if (parameter.getType().equals(HttpRequest.class)) {
            return RequestContext.getInstance().getRequest();
        } else if (parameter.getType().equals(HttpResponse.class)) {
            return RequestContext.getInstance().getResponse();
        } else if (parameter.getType().equals(RequestContext.class)) {
            return RequestContext.getInstance();
        } else if (parameter.isAnnotationPresent(FormData.class)) {
            return deserializeFormDataParameter(parameter, request, postProcessingResults);
        } else if (parameter.isAnnotationPresent(UserId.class)) {
            checkAuthenticated();
            return validateAndRetrieve(userContext::getUserId, "UserId expected, but it was null");
        } else if (parameter.isAnnotationPresent(ClientId.class)) {
            return validateAndRetrieve(request::getClientId, "ClientId expected, but it was null");
        } else if (parameter.isAnnotationPresent(QueryParameter.class)) {
            return deserializeUrlParameter(parameter, request, postProcessingResults);
        } else if (parameter.isAnnotationPresent(one.xis.PathVariable.class)) {
            return deserializePathVariable(parameter, request, postProcessingResults);
        } else if (parameter.isAnnotationPresent(one.xis.Parameter.class)) {
            return deserializeParameter(parameter, request, postProcessingResults);
        } else if (parameter.isAnnotationPresent(SharedValue.class)) {
            var key = parameter.getAnnotation(SharedValue.class).value();
            var sharedValue = requestScope.get(key);
            if (sharedValue == null) {
                if (isMandatory(parameter)) {
                    var defaultValue = createDefault(parameter);
                    requestScope.put(key, defaultValue);
                    return defaultValue;
                } else {
                    return null;
                }
            }
            return sharedValue;
        } else if (parameter.isAnnotationPresent(SessionStorage.class)) {
            var key = parameter.getAnnotation(SessionStorage.class).value();
            var paramValue = request.getSessionStorageData().get(key);
            if (paramValue == null) {
                return isMandatory(parameter) ? createDefault(parameter) : null;
            }
            return deserializeParameter(paramValue, request, parameter, postProcessingResults);
        } else if (parameter.isAnnotationPresent(LocalStorage.class)) {
            var key = parameter.getAnnotation(LocalStorage.class).value();
            var paramValue = request.getLocalStorageData().get(key);
            if (paramValue == null) {
                return isMandatory(parameter) ? createDefault(parameter) : null;
            }
            return deserializeParameter(paramValue, request, parameter, postProcessingResults);
        } else if (parameter.isAnnotationPresent(ClientStorage.class)) {
            var key = parameter.getAnnotation(ClientStorage.class).value();
            var paramValue = request.getClientStorageData().get(key);
            if (paramValue == null) {
                return isMandatory(parameter) ? createDefault(parameter) : null;
            }
            return deserializeParameter(paramValue, request, parameter, postProcessingResults);
        } else if (method.isAnnotationPresent(Action.class)
                && positionalParameterIndex >= 0
                && request.getActionParameters().containsKey("$" + positionalParameterIndex)) {
            var paramValue = request.getActionParameters().get("$" + positionalParameterIndex);
            return deserializeParameter(paramValue, request, parameter, postProcessingResults);
        } else {
            throw new IllegalStateException(method + ": parameter without annotation=" + parameter);
        }
    }

    void addParameterValueToResult(ControllerMethodResult controllerMethodResult, Object parameterValue, ClientRequest request) {
        if (parameter.isAnnotationPresent(one.xis.Parameter.class)) {
            addControllerParameterValueToResult(controllerMethodResult, parameterValue, request);
        } else if (parameter.isAnnotationPresent(FormData.class)) {
            controllerMethodResult.getFormData().put(parameter.getAnnotation(FormData.class).value(), parameterValue);
        } else if (parameter.isAnnotationPresent(QueryParameter.class)) {
            controllerMethodResult.getUrlParameters().put(parameter.getAnnotation(QueryParameter.class).value(), parameterValue);
        } else if (parameter.isAnnotationPresent(PathVariable.class)) {
            controllerMethodResult.getPathVariables().put(parameter.getAnnotation(PathVariable.class).value(), parameterValue);
        } else if (parameter.isAnnotationPresent(SessionStorage.class)) {
            controllerMethodResult.getSessionStorage().put(parameter.getAnnotation(SessionStorage.class).value(), parameterValue);
        } else if (parameter.isAnnotationPresent(LocalStorage.class)) {
            controllerMethodResult.getLocalStorage().put(parameter.getAnnotation(LocalStorage.class).value(), parameterValue);
        } else if (parameter.isAnnotationPresent(ClientStorage.class)) {
            controllerMethodResult.getClientStorage().put(parameter.getAnnotation(ClientStorage.class).value(), parameterValue);
        }
    }

    private void addControllerParameterValueToResult(ControllerMethodResult controllerMethodResult, Object parameterValue, ClientRequest request) {
        var key = parameter.getAnnotation(one.xis.Parameter.class).value();
        if (isActionParameter(request)) {
            if (!key.isEmpty()) {
                controllerMethodResult.getModelData().put(key, parameterValue);
            }
            return;
        }
        if (key.isEmpty() && parameterValue instanceof Map<?, ?> map) {
            map.forEach((name, value) -> controllerMethodResult.getFrontletParameters().put(String.valueOf(name), value));
        } else if (!key.isEmpty()) {
            controllerMethodResult.getFrontletParameters().put(key, parameterValue);
        }
    }

    private String actionParameterKey() {
        var actionParameter = parameter.getAnnotation(one.xis.Parameter.class);
        if (!actionParameter.value().isEmpty()) {
            return actionParameter.value();
        }
        if (actionParameter.index() == 0) {
            throw new IllegalStateException(method + ": @Parameter index is 1-based; use index=1 for the first action argument");
        }
        var index = actionParameter.index() > 0 ? actionParameter.index() - 1 : positionalParameterIndex;
        if (index < 0) {
            throw new IllegalStateException(method + ": positional @Parameter cannot be resolved for " + parameter);
        }
        return "$" + index;
    }

    private void checkAuthenticated() {
        if (!UserContextImpl.getInstance().isAuthenticated()) {
            throw new AuthenticationException("User is not authenticated");
        }
    }

    private Object validateAndRetrieve(Supplier<Object> valueSupplier, String exceptionMessage) {
        var value = valueSupplier.get();
        if (value == null) {
            throw new IllegalStateException(exceptionMessage);
        }
        return value;
    }

    private Object deserializeFormDataParameter(Parameter parameter, ClientRequest request, PostProcessingResults postProcessingResults) throws IOException {
        var key = parameter.getAnnotation(FormData.class).value();
        var paramValue = request.getFormData().get(key);
        if (!isMandatory(parameter) && paramValue == null) {
            return null;
        }
        return deserializeParameter(paramValue, request, parameter, postProcessingResults);
    }

    private Object deserializeUrlParameter(Parameter parameter, ClientRequest request, PostProcessingResults postProcessingResults) throws IOException {
        var key = parameter.getAnnotation(QueryParameter.class).value();
        var paramValue = request.getUrlParameters().get(key);
        if (!isMandatory(parameter) && paramValue == null) {
            return null;
        }
        var deserialized = deserializeParameter(paramValue, request, parameter, postProcessingResults);
        if (deserialized instanceof String str) {
            try {
                return URLDecoder.decode(str, StandardCharsets.UTF_8);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to decode URL parameter '" + key + "' with value '" + str + "'", e);
            }
        } else {
            return deserialized;
        }
    }

    private Object deserializePathVariable(Parameter parameter, ClientRequest request, PostProcessingResults postProcessingResults) throws IOException {
        var key = parameter.getAnnotation(PathVariable.class).value();
        if (isMandatory(parameter) && !request.getPathVariables().containsKey(key)) {
            throw new IllegalStateException("No path variable found for key " + key);
        }
        var paramValue = request.getPathVariables().get(key);
        return deserializeParameter(paramValue, request, parameter, postProcessingResults);
    }

    private Object deserializeParameter(Parameter parameter, ClientRequest request, PostProcessingResults postProcessingResults) throws IOException {
        if (isActionParameter(request)) {
            var paramValue = request.getActionParameters().get(actionParameterKey());
            return deserializeParameter(paramValue, request, parameter, postProcessingResults);
        }
        var key = parameter.getAnnotation(one.xis.Parameter.class).value();
        if (key.isEmpty() && Map.class.isAssignableFrom(parameter.getType())) {
            return deserializeParameterMap(request.getFrontletParameters());
        }
        if (isMandatory(parameter) && !request.getFrontletParameters().containsKey(key)) {
            throw new IllegalStateException("No parameter found for key " + key);
        }
        var paramValue = request.getFrontletParameters().get(key);
        return deserializeParameter(paramValue, request, parameter, postProcessingResults);
    }

    private boolean isActionParameter(ClientRequest request) {
        if (!method.isAnnotationPresent(Action.class)) {
            return false;
        }
        if (!parameter.isAnnotationPresent(one.xis.Parameter.class)) {
            return false;
        }
        var annotation = parameter.getAnnotation(one.xis.Parameter.class);
        if (annotation.value().isEmpty()) {
            return annotation.index() >= 0 || positionalParameterIndex >= 0;
        }
        return request.getActionParameters().containsKey(annotation.value());
    }

    private Map<String, String> deserializeParameterMap(Map<String, String> parameters) {
        var result = new LinkedHashMap<String, String>();
        parameters.forEach((key, value) -> result.put(key, deserializeParameterMapValue(value)));
        return result;
    }

    private String deserializeParameterMapValue(String value) {
        if (value == null) {
            return null;
        }
        try {
            var element = JsonParser.parseString(value);
            if (element.isJsonPrimitive()) {
                return element.getAsString();
            }
            return element.toString();
        } catch (Exception e) {
            return value;
        }
    }

    private Object deserializeParameter(String jsonValue, ClientRequest request, Parameter parameter, PostProcessingResults postProcessingResults) throws IOException {
        if (!isMandatory(parameter) && jsonValue == null) {
            return null;
        }
        var userContext = UserContextImpl.getInstance();
        return deserializer.deserialize(jsonValue, parameter, userContext, postProcessingResults);
    }

    private boolean isMandatory(Parameter parameter) {
        return !parameter.isAnnotationPresent(NullAllowed.class);
    }


    private Object createDefault(Parameter parameter) {
        try {
            return ClassUtils.newInstance(parameter.getType());
        } catch (Exception e) {
            throw new IllegalStateException("Cannot create default instance of type " + parameter.getType());
        }
    }

}
