package one.xis.server;

import lombok.RequiredArgsConstructor;
import one.xis.*;
import one.xis.PathVariable;
import one.xis.auth.token.AccessToken;
import one.xis.deserialize.MainDeserializer;
import one.xis.deserialize.PostProcessingResults;
import one.xis.security.AuthenticationException;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.ZoneId;
import java.util.Map;
import java.util.function.Supplier;

@RequiredArgsConstructor
class ControllerMethodParameter {
    private final Method method;
    private final Parameter parameter;
    private final MainDeserializer deserializer;

    // TODO Validation: only one of these annotation in parameter
    Object prepareParameter(ClientRequest request, PostProcessingResults postProcessingResults, Map<String, Object> requestScope, AccessToken accessToken) throws Exception {
        if (parameter.isAnnotationPresent(FormData.class)) {
            return deserializeFormDataParameter(parameter, request, postProcessingResults, accessToken);
        } else if (parameter.isAnnotationPresent(UserId.class)) {
            checkAccessToken(accessToken);
            return validateAndRetrieve(accessToken::getUserId, "UserId expected, but it was null");
        } else if (parameter.isAnnotationPresent(ClientId.class)) {
            return validateAndRetrieve(request::getClientId, "ClientId expected, but it was null");
        } else if (parameter.isAnnotationPresent(URLParameter.class)) {
            return deserializeUrlParameter(parameter, request, postProcessingResults, accessToken);
        } else if (parameter.isAnnotationPresent(one.xis.PathVariable.class)) {
            return deserializePathVariable(parameter, request, postProcessingResults, accessToken);
        } else if (parameter.isAnnotationPresent(WidgetParameter.class)) {
            return deserializeWidgetParameter(parameter, request, postProcessingResults, accessToken);
        } else if (parameter.isAnnotationPresent(ActionParameter.class)) {
            var key = parameter.getAnnotation(ActionParameter.class).value();
            var paramValue = request.getActionParameters().get(key);
            return deserializeParameter(paramValue, request, parameter, postProcessingResults, accessToken);
        } else if (parameter.isAnnotationPresent(RequestScope.class)) {
            var key = parameter.getAnnotation(RequestScope.class).value();
            var paramValue = requestScope.get(key);
            if (paramValue == null) {
                throw new IllegalStateException(method + ": No request scope value found for key " + key);
            }
            return paramValue;
        } else if (parameter.isAnnotationPresent(ClientState.class)) {
            var key = parameter.getAnnotation(ClientState.class).value();
            var paramValue = request.getClientStateData().get(key);
            return deserializeParameter(paramValue, request, parameter, postProcessingResults, accessToken);
        } else if (parameter.isAnnotationPresent(LocalStorage.class)) {
            var key = parameter.getAnnotation(LocalStorage.class).value();
            var paramValue = request.getLocalStorageData().get(key);
            return deserializeParameter(paramValue, request, parameter, postProcessingResults, accessToken);
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
        } else if (parameter.isAnnotationPresent(WidgetParameter.class)) {
            controllerMethodResult.getWidgetParameters().put(parameter.getAnnotation(WidgetParameter.class).value(), parameterValue);
        } else if (parameter.isAnnotationPresent(RequestScope.class)) {
            controllerMethodResult.getRequestScope().put(parameter.getAnnotation(RequestScope.class).value(), parameterValue);
        } else if (parameter.isAnnotationPresent(ClientState.class)) {
            controllerMethodResult.getClientState().put(parameter.getAnnotation(ClientState.class).value(), parameterValue);
        } else if (parameter.isAnnotationPresent(LocalStorage.class)) {
            controllerMethodResult.getLocalStorage().put(parameter.getAnnotation(LocalStorage.class).value(), parameterValue);
        } else {
            throw new IllegalStateException(method + ": parameter without annotation=" + parameter);
        }
    }

    private void checkAccessToken(AccessToken accessToken) {
        if (accessToken == null) {
            throw new AuthenticationException("UserId required for method " + method + ", but no access token provided");
        }
        if (accessToken.isAuthenticated()) {
            throw new AuthenticationException("UserId required for method " + method + ", but access token is not authenticated");
        }
    }

    private Object validateAndRetrieve(Supplier<Object> valueSupplier, String exceptionMessage) {
        var value = valueSupplier.get();
        if (value == null) {
            throw new IllegalStateException(exceptionMessage);
        }
        return value;
    }

    private Object deserializeFormDataParameter(Parameter parameter, ClientRequest request, PostProcessingResults postProcessingResults, AccessToken accessToken) throws IOException {
        var key = parameter.getAnnotation(FormData.class).value();
        var paramValue = request.getFormData().get(key);
        return deserializeParameter(paramValue, request, parameter, postProcessingResults, accessToken);
    }

    private Object deserializeUrlParameter(Parameter parameter, ClientRequest request, PostProcessingResults postProcessingResults, AccessToken accessToken) throws IOException {
        var key = parameter.getAnnotation(URLParameter.class).value();
        var paramValue = request.getUrlParameters().get(key);
        return deserializeParameter(paramValue, request, parameter, postProcessingResults, accessToken);
    }

    private Object deserializePathVariable(Parameter parameter, ClientRequest request, PostProcessingResults postProcessingResults, AccessToken accessToken) throws IOException {
        var key = parameter.getAnnotation(PathVariable.class).value();
        if (!request.getPathVariables().containsKey(key)) {
            throw new IllegalStateException("No path variable found for key " + key);
        }
        var paramValue = request.getPathVariables().get(key);
        return deserializeParameter(paramValue, request, parameter, postProcessingResults, accessToken);
    }

    private Object deserializeWidgetParameter(Parameter parameter, ClientRequest request, PostProcessingResults postProcessingResults, AccessToken accessToken) throws IOException {
        var key = parameter.getAnnotation(WidgetParameter.class).value();
        if (!request.getBindingParameters().containsKey(key)) {
            throw new IllegalStateException("No widget parameter found for key " + key);
        }
        var paramValue = request.getBindingParameters().get(key);
        return deserializeParameter(paramValue, request, parameter, postProcessingResults, accessToken);
    }

    private Object deserializeParameter(String jsonValue, ClientRequest request, Parameter parameter, PostProcessingResults postProcessingResults, AccessToken accessToken) throws IOException {
        if (jsonValue == null) {
            return null;
        }
        var userContext = new UserContextImpl(request.getLocale(), ZoneId.of(request.getZoneId()), request.getClientId(), accessToken);
        return deserializer.deserialize(jsonValue, parameter, userContext, postProcessingResults);
    }


}
