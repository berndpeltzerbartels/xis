package one.xis.server;

import lombok.RequiredArgsConstructor;
import one.xis.*;
import one.xis.PathVariable;
import one.xis.deserialize.MainDeserializer;
import one.xis.deserialize.PostProcessingResults;

import java.io.IOException;
import java.lang.reflect.Method;
import java.time.ZoneId;
import java.util.Map;
import java.util.function.Supplier;

@RequiredArgsConstructor
class ControllerMethodParameter {
    private final Method method;
    private final java.lang.reflect.Parameter parameter;
    private final MainDeserializer deserializer;

    // TODO Validation: only one of these annotation in parameter
    Object prepareParameter(ClientRequest request, PostProcessingResults postProcessingResults, Map<String, Object> requestScope) throws Exception {
        if (parameter.isAnnotationPresent(FormData.class)) {
            return deserializeFormDataParameter(parameter, request, postProcessingResults);
        } else if (parameter.isAnnotationPresent(UserId.class)) {
            return validateAndRetrieve(request::getUserId, "UserId expected, but it was null");
        } else if (parameter.isAnnotationPresent(ClientId.class)) {
            return validateAndRetrieve(request::getClientId, "ClientId expected, but it was null");
        } else if (parameter.isAnnotationPresent(URLParameter.class)) {
            return deserializeUrlParameter(parameter, request, postProcessingResults);
        } else if (parameter.isAnnotationPresent(one.xis.PathVariable.class)) {
            return deserializePathVariable(parameter, request, postProcessingResults);
        } else if (parameter.isAnnotationPresent(Parameter.class)) {
            return deserializeWidgetParameter(parameter, request, postProcessingResults);
        } else if (parameter.isAnnotationPresent(ActionParameter.class)) {
            var key = parameter.getAnnotation(ActionParameter.class).value();
            var paramValue = request.getActionParameters().get(key);
            return deserializeParameter(paramValue, request, parameter, postProcessingResults);
        } else if (parameter.isAnnotationPresent(RequestScope.class)) {
            var key = parameter.getAnnotation(RequestScope.class).value();
            var paramValue = requestScope.get(key);
            if (paramValue == null) {
                throw new IllegalStateException(method + ": No request scope value found for key " + key);
            }
            return paramValue;
        } else if (parameter.isAnnotationPresent(ClientScope.class)) {
            var key = parameter.getAnnotation(ClientScope.class).value();
            var paramValue = request.getClientScope().get(key);
            return deserializeParameter(paramValue, request, parameter, postProcessingResults);
        } else if (parameter.isAnnotationPresent(LocalStorage.class)) {
            var key = parameter.getAnnotation(LocalStorage.class).value();
            var paramValue = request.getLocalStorage().get(key);
            return deserializeParameter(paramValue, request, parameter, postProcessingResults);
        } else {
            throw new IllegalStateException(method + ": parameter without annotation=" + parameter);
        }
    }

    void addParameterValueToResult(ControllerMethodResult controllerMethodResult, Object parameterValue) {
        if (parameter.isAnnotationPresent(ActionParameter.class)) {
            controllerMethodResult.getModelData().put(parameter.getAnnotation(ActionParameter.class).value(), parameterValue);
        } else if (parameter.isAnnotationPresent(FormData.class)) {
            controllerMethodResult.getFormData().put(parameter.getAnnotation(FormData.class).value(), parameterValue);
        } else if (parameter.isAnnotationPresent(URLParameter.class)) {
            controllerMethodResult.getUrlParameters().put(parameter.getAnnotation(URLParameter.class).value(), parameterValue);
        } else if (parameter.isAnnotationPresent(PathVariable.class)) {
            controllerMethodResult.getPathVariables().put(parameter.getAnnotation(PathVariable.class).value(), parameterValue);
        } else if (parameter.isAnnotationPresent(Parameter.class)) {
            controllerMethodResult.getWidgetParameters().put(parameter.getAnnotation(Parameter.class).value(), parameterValue);
        } else if (parameter.isAnnotationPresent(RequestScope.class)) {
            controllerMethodResult.getRequestScope().put(parameter.getAnnotation(RequestScope.class).value(), parameterValue);
        } else if (parameter.isAnnotationPresent(ClientScope.class)) {
            controllerMethodResult.getClientScope().put(parameter.getAnnotation(ClientScope.class).value(), parameterValue);
        } else if (parameter.isAnnotationPresent(LocalStorage.class)) {
            controllerMethodResult.getLocalStorage().put(parameter.getAnnotation(LocalStorage.class).value(), parameterValue);
        } else {
            throw new IllegalStateException(method + ": parameter without annotation=" + parameter);
        }
    }

    private Object validateAndRetrieve(Supplier<Object> valueSupplier, String exceptionMessage) {
        var value = valueSupplier.get();
        if (value == null) {
            throw new IllegalStateException(exceptionMessage);
        }
        return value;
    }

    private Object deserializeFormDataParameter(java.lang.reflect.Parameter parameter, ClientRequest request, PostProcessingResults postProcessingResults) throws IOException {
        var key = parameter.getAnnotation(FormData.class).value();
        var paramValue = request.getFormData().get(key);
        return deserializeParameter(paramValue, request, parameter, postProcessingResults);
    }

    private Object deserializeUrlParameter(java.lang.reflect.Parameter parameter, ClientRequest request, PostProcessingResults postProcessingResults) throws IOException {
        var key = parameter.getAnnotation(URLParameter.class).value();
        var paramValue = request.getUrlParameters().get(key);
        return deserializeParameter(paramValue, request, parameter, postProcessingResults);
    }

    private Object deserializePathVariable(java.lang.reflect.Parameter parameter, ClientRequest request, PostProcessingResults postProcessingResults) throws IOException {
        var key = parameter.getAnnotation(PathVariable.class).value();
        if (!request.getPathVariables().containsKey(key)) {
            throw new IllegalStateException("No path variable found for key " + key);
        }
        var paramValue = request.getPathVariables().get(key);
        return deserializeParameter(paramValue, request, parameter, postProcessingResults);
    }

    private Object deserializeWidgetParameter(java.lang.reflect.Parameter parameter, ClientRequest request, PostProcessingResults postProcessingResults) throws IOException {
        var key = parameter.getAnnotation(Parameter.class).value();
        if (!request.getBindingParameters().containsKey(key)) {
            throw new IllegalStateException("No widget parameter found for key " + key);
        }
        var paramValue = request.getBindingParameters().get(key);
        return deserializeParameter(paramValue, request, parameter, postProcessingResults);
    }

    private Object deserializeParameter(String jsonValue, ClientRequest request, java.lang.reflect.Parameter parameter, PostProcessingResults postProcessingResults) throws IOException {
        if (jsonValue == null) {
            return null;
        }
        var userContext = new UserContext(request.getLocale(), ZoneId.of(request.getZoneId()), request.getUserId(), request.getClientId());
        return deserializer.deserialize(jsonValue, parameter, userContext, postProcessingResults);
    }


}
