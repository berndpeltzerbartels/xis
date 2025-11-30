package one.xis.server;

import lombok.RequiredArgsConstructor;
import one.xis.*;
import one.xis.PathVariable;
import one.xis.auth.AuthenticationException;
import one.xis.deserialize.MainDeserializer;
import one.xis.deserialize.PostProcessingResults;
import one.xis.http.HttpRequest;
import one.xis.http.HttpResponse;
import one.xis.http.RequestContext;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Supplier;

@RequiredArgsConstructor
class ControllerMethodParameter {
    private final Method method;
    private final Parameter parameter;
    private final MainDeserializer deserializer;

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
        } else if (parameter.isAnnotationPresent(WidgetParameter.class)) {
            return deserializeWidgetParameter(parameter, request, postProcessingResults);
        } else if (parameter.isAnnotationPresent(ActionParameter.class)) {
            var key = parameter.getAnnotation(ActionParameter.class).value();
            var paramValue = request.getActionParameters().get(key);
            return deserializeParameter(paramValue, request, parameter, postProcessingResults);
        } else if (parameter.isAnnotationPresent(MethodParameter.class)) {
            var key = parameter.getAnnotation(MethodParameter.class).value();
            return requestScope.get(key);
        } else if (parameter.isAnnotationPresent(SessionStorage.class)) {
            var key = parameter.getAnnotation(SessionStorage.class).value();
            var paramValue = request.getSessionStorageData().get(key);
            return deserializeParameter(paramValue, request, parameter, postProcessingResults);
        } else if (parameter.isAnnotationPresent(LocalStorage.class)) {
            var key = parameter.getAnnotation(LocalStorage.class).value();
            var paramValue = request.getLocalStorageData().get(key);
            return deserializeParameter(paramValue, request, parameter, postProcessingResults);
        } else if (parameter.isAnnotationPresent(ClientStorage.class)) {
            var key = parameter.getAnnotation(ClientStorage.class).value();
            var paramValue = request.getClientStorageData().get(key);
            return deserializeParameter(paramValue, request, parameter, postProcessingResults);
        } else if (parameter.isAnnotationPresent(GlobalVariable.class)) {
            var key = parameter.getAnnotation(GlobalVariable.class).value();
            var paramValue = request.getGlobalVariableData().get(key);
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
        } else if (parameter.isAnnotationPresent(QueryParameter.class)) {
            controllerMethodResult.getUrlParameters().put(parameter.getAnnotation(QueryParameter.class).value(), parameterValue);
        } else if (parameter.isAnnotationPresent(PathVariable.class)) {
            controllerMethodResult.getPathVariables().put(parameter.getAnnotation(PathVariable.class).value(), parameterValue);
        } else if (parameter.isAnnotationPresent(WidgetParameter.class)) {
            controllerMethodResult.getWidgetParameters().put(parameter.getAnnotation(WidgetParameter.class).value(), parameterValue);
        } else if (parameter.isAnnotationPresent(MethodParameter.class)) {
            controllerMethodResult.getRequestScope().put(parameter.getAnnotation(MethodParameter.class).value(), parameterValue);
        } else if (parameter.isAnnotationPresent(SessionStorage.class)) {
            controllerMethodResult.getSessionStorage().put(parameter.getAnnotation(SessionStorage.class).value(), parameterValue);
        } else if (parameter.isAnnotationPresent(LocalStorage.class)) {
            controllerMethodResult.getLocalStorage().put(parameter.getAnnotation(LocalStorage.class).value(), parameterValue);
        } else if (parameter.isAnnotationPresent(ClientStorage.class)) {
            controllerMethodResult.getClientStorage().put(parameter.getAnnotation(ClientStorage.class).value(), parameterValue);
        } else if (parameter.isAnnotationPresent(GlobalVariable.class)) {
            controllerMethodResult.getGlobalVariables().put(parameter.getAnnotation(GlobalVariable.class).value(), parameterValue);
        } else {
            throw new IllegalStateException(method + ": parameter without annotation=" + parameter);
        }
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
        return deserializeParameter(paramValue, request, parameter, postProcessingResults);
    }

    private Object deserializeUrlParameter(Parameter parameter, ClientRequest request, PostProcessingResults postProcessingResults) throws IOException {
        var key = parameter.getAnnotation(QueryParameter.class).value();
        var paramValue = request.getUrlParameters().get(key);
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
        if (!request.getPathVariables().containsKey(key)) {
            throw new IllegalStateException("No path variable found for key " + key);
        }
        var paramValue = request.getPathVariables().get(key);
        return deserializeParameter(paramValue, request, parameter, postProcessingResults);
    }

    private Object deserializeWidgetParameter(Parameter parameter, ClientRequest request, PostProcessingResults postProcessingResults) throws IOException {
        var key = parameter.getAnnotation(WidgetParameter.class).value();
        if (!request.getBindingParameters().containsKey(key)) {
            throw new IllegalStateException("No widget parameter found for key " + key);
        }
        var paramValue = request.getBindingParameters().get(key);
        return deserializeParameter(paramValue, request, parameter, postProcessingResults);
    }

    private Object deserializeParameter(String jsonValue, ClientRequest request, Parameter parameter, PostProcessingResults postProcessingResults) throws IOException {
        if (jsonValue == null) {
            return null;
        }
        var userContext = UserContextImpl.getInstance();
        return deserializer.deserialize(jsonValue, parameter, userContext, postProcessingResults);
    }


}
