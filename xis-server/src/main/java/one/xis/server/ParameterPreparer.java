package one.xis.server;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.xis.PathVariable;
import one.xis.*;
import one.xis.context.XISComponent;
import one.xis.parameter.ParameterDeserializer;
import one.xis.validation.ValidationFailedException;
import one.xis.validation.ValidatorResultElement;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.ZoneId;
import java.util.Map;
import java.util.Objects;

@Slf4j
@XISComponent
@RequiredArgsConstructor
class ParameterPreparer {
    private final ParameterDeserializer parameterDeserializer;

    Object[] prepareParameters(Method method, ClientRequest context, Map<String, ValidationError> errors) throws Exception {
        var rootValidationResult = ValidatorResultElement.rootResult();
        Object[] args = new Object[method.getParameterCount()];
        var params = method.getParameters();
        for (int i = 0; i < args.length; i++) {
            var param = params[i];
            if (param.isAnnotationPresent(ModelData.class)) {
                args[i] = deserializeModelParameter(param, context, errors);
            } else if (param.isAnnotationPresent(FormData.class)) {
                args[i] = deserializeFormDataParameter(param, context, errors);
            } else if (param.isAnnotationPresent(UserId.class)) {
                args[i] = Objects.requireNonNull(context.getUserId(), "UserId expected, but it was null"); // TODO Specialized exception and login
            } else if (param.isAnnotationPresent(ClientId.class)) {
                args[i] = Objects.requireNonNull(context.getClientId(), "ClientId expected, but it was null");
            } else if (param.isAnnotationPresent(URLParameter.class)) {
                args[i] = deserializeUrlParameter(param, context, errors);
            } else if (param.isAnnotationPresent(one.xis.PathVariable.class)) {
                args[i] = deserializePathVariable(param, context, errors);
            } else if (param.isAnnotationPresent(WidgetParameter.class)) {
                args[i] = deserializeWidgetParameter(param, context, errors);
            } else {
                throw new IllegalStateException(method + ": parameter without annotation=" + param);
            }
        }
        if (rootValidationResult.hasErrors()) {
            throw new ValidationFailedException(rootValidationResult.getErrors());
        }
        return args;
    }

    private Object deserializeModelParameter(Parameter parameter, ClientRequest request, Map<String, ValidationError> errors) throws IOException {
        var key = parameter.getAnnotation(ModelData.class).value();
        var paramValue = request.getData().get(key);
        return deserializeParameter(paramValue, request, parameter, errors);
    }

    private Object deserializeFormDataParameter(Parameter parameter, ClientRequest request, Map<String, ValidationError> errors) throws IOException {
        var key = parameter.getAnnotation(FormData.class).value();
        var paramValue = request.getFormData().get(key);
        return deserializeParameter(paramValue, request, parameter, errors);
    }

    private Object deserializeUrlParameter(Parameter parameter, ClientRequest request, Map<String, ValidationError> errors) throws IOException {
        var key = parameter.getAnnotation(URLParameter.class).value();
        var paramValue = request.getUrlParameters().get(key);
        return deserializeParameter(paramValue, request, parameter, errors);
    }

    private Object deserializePathVariable(Parameter parameter, ClientRequest request, Map<String, ValidationError> errors) throws IOException {
        var key = parameter.getAnnotation(PathVariable.class).value();
        var paramValue = request.getPathVariables().get(key);
        return deserializeParameter(paramValue, request, parameter, errors);
    }

    private Object deserializeWidgetParameter(Parameter parameter, ClientRequest request, Map<String, ValidationError> errors) throws IOException {
        var key = parameter.getAnnotation(WidgetParameter.class).value();
        var paramValue = request.getWidgetParameters().get(key);
        return deserializeParameter(paramValue, request, parameter, errors);
    }

    private Object deserializeParameter(String jsonValue, ClientRequest request, Parameter parameter, Map<String, ValidationError> errors) throws IOException {
        var userContext = new UserContext(request.getLocale(), ZoneId.of(request.getZoneId()), request.getUserId(), request.getClientId());
        return parameterDeserializer.deserialize(jsonValue, parameter, errors, userContext);
    }


}
