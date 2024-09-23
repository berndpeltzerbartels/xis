package one.xis.server;

import lombok.RequiredArgsConstructor;
import one.xis.PathVariable;
import one.xis.*;
import one.xis.deserialize.MainDeserializer;
import one.xis.deserialize.PostProcessingResults;

import java.io.IOException;
import java.lang.reflect.Method;
import java.time.ZoneId;
import java.util.Objects;

@RequiredArgsConstructor
class ControllerMethodParameter {
    private final Method method;
    private final java.lang.reflect.Parameter parameter;
    private final MainDeserializer deserializer;

    Object prepareParameter(ClientRequest request, PostProcessingResults postProcessingResults) throws Exception {
        if (parameter.isAnnotationPresent(FormData.class)) {
            return deserializeFormDataParameter(parameter, request, postProcessingResults);
        } else if (parameter.isAnnotationPresent(UserId.class)) {
            return Objects.requireNonNull(request.getUserId(), "UserId expected, but it was null"); // TODO Specialized exception and login
        } else if (parameter.isAnnotationPresent(ClientId.class)) {
            return Objects.requireNonNull(request.getClientId(), "ClientId expected, but it was null");
        } else if (parameter.isAnnotationPresent(URLParameter.class)) {
            return deserializeUrlParameter(parameter, request, postProcessingResults);
        } else if (parameter.isAnnotationPresent(one.xis.PathVariable.class)) {
            return deserializePathVariable(parameter, request, postProcessingResults);
        } else if (parameter.isAnnotationPresent(WidgetParameter.class)) {
            return deserializeWidgetParameter(parameter, request, postProcessingResults);
        } else if (parameter.isAnnotationPresent(ActionParameter.class)) {
            var key = parameter.getAnnotation(ActionParameter.class).value();
            var paramValue = request.getActionParameters().get(key);
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
        } else if (parameter.isAnnotationPresent(WidgetParameter.class)) {
            controllerMethodResult.getWidgetParameters().put(parameter.getAnnotation(WidgetParameter.class).value(), parameterValue);
        } else {
            throw new IllegalStateException(method + ": parameter without annotation=" + parameter);
        }
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
        var key = parameter.getAnnotation(WidgetParameter.class).value();
        if (!request.getWidgetParameters().containsKey(key)) {
            throw new IllegalStateException("No widget parameter found for key " + key);
        }
        var paramValue = request.getWidgetParameters().get(key);
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
