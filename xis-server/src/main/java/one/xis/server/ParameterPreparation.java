package one.xis.server;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.xis.PathVariable;
import one.xis.*;
import one.xis.context.XISComponent;
import one.xis.utils.lang.CollectionUtils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.Objects;

@Slf4j
@XISComponent
@RequiredArgsConstructor
class ParameterPreparation {
    private final JsonDeserializer jsonDeserializer;
    private final Validation validation;

    Object[] prepareParameters(Method method, ClientRequest context) throws Exception {
        var rootValidationResult = ValidatorResultElement.rootResult();
        Object[] args = new Object[method.getParameterCount()];
        var params = method.getParameters();
        for (int i = 0; i < args.length; i++) {
            var param = params[i];
            if (param.isAnnotationPresent(ModelData.class)) {
                args[i] = deserializeModelParameter(param, context, rootValidationResult);
            } else if (param.isAnnotationPresent(FormData.class)) {
                args[i] = deserializeFormDataParameter(param, context, rootValidationResult);
            } else if (param.isAnnotationPresent(UserId.class)) {
                args[i] = Objects.requireNonNull(context.getUserId(), "UserId expected, but it was null"); // TODO Specialized exception and login
            } else if (param.isAnnotationPresent(ClientId.class)) {
                args[i] = Objects.requireNonNull(context.getClientId(), "ClientId expected, but it was null");
            } else if (param.isAnnotationPresent(URLParameter.class)) {
                var key = param.getAnnotation(URLParameter.class).value();
                args[i] = deserializeUrlParameter(param, context, rootValidationResult);
            } else if (param.isAnnotationPresent(one.xis.PathVariable.class)) {
                var key = param.getAnnotation(PathVariable.class).value();
                args[i] = deserializePathVariable(param, context, rootValidationResult);
            } else if (param.isAnnotationPresent(WidgetParameter.class)) {
                var key = param.getAnnotation(WidgetParameter.class).value();
                args[i] = deserializeWidgetParameter(param, context, rootValidationResult);
            } else {
                throw new IllegalStateException(method + ": parameter without annotation=" + param);
            }
        }
        return args;
    }

    private Object deserializeModelParameter(Parameter parameter, ClientRequest context, ValidatorResultElement resultElement) throws IOException {
        var key = parameter.getAnnotation(ModelData.class).value();
        var paramValue = context.getData().get(key);
        return deserializeParameter(paramValue, parameter, resultElement);
    }

    private Object deserializeFormDataParameter(Parameter parameter, ClientRequest context, ValidatorResultElement validatorResultElement) throws IOException {
        var key = parameter.getAnnotation(FormData.class).value();
        var paramValue = context.getFormData().get(key);
        return deserializeParameter(paramValue, parameter, validatorResultElement);
    }

    private Object deserializeUrlParameter(Parameter parameter, ClientRequest context, ValidatorResultElement resultElement) throws IOException {
        var key = parameter.getAnnotation(URLParameter.class).value();
        var paramValue = context.getFormData().get(key);
        return deserializeParameter(paramValue, parameter, resultElement);
    }

    private Object deserializePathVariable(Parameter parameter, ClientRequest context, ValidatorResultElement resultElement) throws IOException {
        var key = parameter.getAnnotation(PathVariable.class).value();
        var paramValue = context.getFormData().get(key);
        return deserializeParameter(paramValue, parameter, resultElement);
    }

    private Object deserializeWidgetParameter(Parameter parameter, ClientRequest context, ValidatorResultElement resultElement) throws IOException {
        var key = parameter.getAnnotation(PathVariable.class).value();
        var paramValue = context.getFormData().get(key);
        return deserializeParameter(paramValue, parameter, resultElement);
    }


    @SuppressWarnings("unchecked")
    private Object deserializeParameter(String paramValue, Parameter parameter, ValidatorResultElement validatorResultElement) throws IOException {
        var target = new JsonDeserializer.TargetParameter(parameter);
        if (paramValue == null) {
            if (Collection.class.isAssignableFrom(parameter.getType())) {
                var collection = CollectionUtils.emptyInstance((Class<Collection<?>>) parameter.getType());
                validation.validateBeforeAssignment(target, collection, validatorResultElement);
                return collection;
            }
            return null;
        } else if (String.class.isAssignableFrom(parameter.getType())) {
            validation.validateBeforeAssignment(target, "", validatorResultElement);
            return null;
        }
        return jsonDeserializer.deserialze(paramValue, parameter, validatorResultElement);
    }
}
