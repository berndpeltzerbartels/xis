package one.xis.server;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.xis.PathVariable;
import one.xis.*;
import one.xis.context.XISComponent;
import one.xis.parameter.ParameterDeserializer;
import one.xis.validation.ValidationErrors;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.ZoneId;
import java.util.Objects;

@Slf4j
@XISComponent
@RequiredArgsConstructor
class ParameterPreparer {
    private final ParameterDeserializer parameterDeserializer;

    Object[] prepareParameters(Method method, ClientRequest request, ValidationErrors errors) throws Exception {
        Object[] args = new Object[method.getParameterCount()];
        var params = method.getParameters();
        for (int i = 0; i < args.length; i++) {
            var param = params[i];
            if (param.isAnnotationPresent(ModelData.class)) {
                args[i] = deserializeModelParameter(param, request, errors);
            } else if (param.isAnnotationPresent(FormData.class)) {
                args[i] = deserializeFormDataParameter(param, request, errors);
            } else if (param.isAnnotationPresent(UserId.class)) {
                args[i] = Objects.requireNonNull(request.getUserId(), "UserId expected, but it was null"); // TODO Specialized exception and login
            } else if (param.isAnnotationPresent(ClientId.class)) {
                args[i] = Objects.requireNonNull(request.getClientId(), "ClientId expected, but it was null");
            } else if (param.isAnnotationPresent(URLParameter.class)) {
                args[i] = deserializeUrlParameter(param, request, errors);
            } else if (param.isAnnotationPresent(one.xis.PathVariable.class)) {
                args[i] = deserializePathVariable(param, request, errors);
            } else if (param.isAnnotationPresent(WidgetParameter.class)) {
                args[i] = deserializeWidgetParameter(param, request, errors);
            } else {
                throw new IllegalStateException(method + ": parameter without annotation=" + param);
            }
        }
        return args;
    }

    private Object deserializeModelParameter(Parameter parameter, ClientRequest request, ValidationErrors errors) throws IOException {
        var key = parameter.getAnnotation(ModelData.class).value();
        var paramValue = request.getData().get(key);
        return deserializeParameter(paramValue, request, parameter, errors);
    }

    private Object deserializeFormDataParameter(Parameter parameter, ClientRequest request, ValidationErrors errors) throws IOException {
        var key = parameter.getAnnotation(FormData.class).value();
        var paramValue = request.getFormData().get(key);
        return deserializeParameter(paramValue, request, parameter, errors);
    }

    private Object deserializeUrlParameter(Parameter parameter, ClientRequest request, ValidationErrors errors) throws IOException {
        var key = parameter.getAnnotation(URLParameter.class).value();
        var paramValue = request.getUrlParameters().get(key);
        return deserializeParameter(paramValue, request, parameter, errors);
    }

    private Object deserializePathVariable(Parameter parameter, ClientRequest request, ValidationErrors errors) throws IOException {
        var key = parameter.getAnnotation(PathVariable.class).value();
        if (!request.getPathVariables().containsKey(key)) {
            throw new IllegalStateException("No path variable found for key " + key);
        }
        var paramValue = request.getPathVariables().get(key);
        return deserializeParameter(paramValue, request, parameter, errors);
    }

    private Object deserializeWidgetParameter(Parameter parameter, ClientRequest request, ValidationErrors errors) throws IOException {
        var key = parameter.getAnnotation(WidgetParameter.class).value();
        if (!request.getWidgetParameters().containsKey(key)) {
            throw new IllegalStateException("No widget parameter found for key " + key);
        }
        var paramValue = request.getWidgetParameters().get(key);
        return deserializeParameter(paramValue, request, parameter, errors);
    }

    private Object deserializeParameter(String jsonValue, ClientRequest request, Parameter parameter, ValidationErrors errors) throws IOException {
        if (jsonValue == null) {
            return null;
        }
        var userContext = new UserContext(request.getLocale(), ZoneId.of(request.getZoneId()), request.getUserId(), request.getClientId());
        return parameterDeserializer.deserialize(jsonValue, parameter, errors, userContext);
    }


}
