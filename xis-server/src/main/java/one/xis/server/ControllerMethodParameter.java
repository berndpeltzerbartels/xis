package one.xis.server;

import lombok.RequiredArgsConstructor;
import one.xis.PathVariable;
import one.xis.*;
import one.xis.deserialize.MainDeserializer;
import one.xis.deserialize.ReportedError;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Objects;

@RequiredArgsConstructor
class ControllerMethodParameter {
    private final Method method;
    private final Parameter parameter;
    private final MainDeserializer deserializer;
    
    Object prepareParameter(ClientRequest request, Collection<ReportedError> errors) throws Exception {
        if (parameter.isAnnotationPresent(ModelData.class)) {
            return deserializeModelParameter(parameter, request, errors);
        } else if (parameter.isAnnotationPresent(FormData.class)) {
            return deserializeFormDataParameter(parameter, request, errors);
        } else if (parameter.isAnnotationPresent(UserId.class)) {
            return Objects.requireNonNull(request.getUserId(), "UserId expected, but it was null"); // TODO Specialized exception and login
        } else if (parameter.isAnnotationPresent(ClientId.class)) {
            return Objects.requireNonNull(request.getClientId(), "ClientId expected, but it was null");
        } else if (parameter.isAnnotationPresent(URLParameter.class)) {
            return deserializeUrlParameter(parameter, request, errors);
        } else if (parameter.isAnnotationPresent(one.xis.PathVariable.class)) {
            return deserializePathVariable(parameter, request, errors);
        } else if (parameter.isAnnotationPresent(WidgetParameter.class)) {
            return deserializeWidgetParameter(parameter, request, errors);
        } else {
            throw new IllegalStateException(method + ": parameter without annotation=" + parameter);
        }
    }

    private Object deserializeModelParameter(Parameter parameter, ClientRequest request, Collection<ReportedError> errors) throws IOException {
        var key = parameter.getAnnotation(ModelData.class).value();
        var paramValue = request.getData().get(key);
        return deserializeParameter(paramValue, request, parameter, errors);
    }

    private Object deserializeFormDataParameter(Parameter parameter, ClientRequest request, Collection<ReportedError> errors) throws IOException {
        var key = parameter.getAnnotation(FormData.class).value();
        var paramValue = request.getFormData().get(key);
        return deserializeParameter(paramValue, request, parameter, errors);
    }

    private Object deserializeUrlParameter(Parameter parameter, ClientRequest request, Collection<ReportedError> errors) throws IOException {
        var key = parameter.getAnnotation(URLParameter.class).value();
        var paramValue = request.getUrlParameters().get(key);
        return deserializeParameter(paramValue, request, parameter, errors);
    }

    private Object deserializePathVariable(Parameter parameter, ClientRequest request, Collection<ReportedError> errors) throws IOException {
        var key = parameter.getAnnotation(PathVariable.class).value();
        if (!request.getPathVariables().containsKey(key)) {
            throw new IllegalStateException("No path variable found for key " + key);
        }
        var paramValue = request.getPathVariables().get(key);
        return deserializeParameter(paramValue, request, parameter, errors);
    }

    private Object deserializeWidgetParameter(Parameter parameter, ClientRequest request, Collection<ReportedError> errors) throws IOException {
        var key = parameter.getAnnotation(WidgetParameter.class).value();
        if (!request.getWidgetParameters().containsKey(key)) {
            throw new IllegalStateException("No widget parameter found for key " + key);
        }
        var paramValue = request.getWidgetParameters().get(key);
        return deserializeParameter(paramValue, request, parameter, errors);
    }

    private Object deserializeParameter(String jsonValue, ClientRequest request, Parameter parameter, Collection<ReportedError> errors) throws IOException {
        if (jsonValue == null) {
            return null;
        }
        var userContext = new UserContext(request.getLocale(), ZoneId.of(request.getZoneId()), request.getUserId(), request.getClientId());
        return deserializer.deserialize(jsonValue, parameter, userContext, errors);
    }


}
