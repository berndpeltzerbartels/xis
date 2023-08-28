package one.xis.server;

import lombok.Data;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import one.xis.PathVariable;
import one.xis.*;
import one.xis.utils.lang.ClassUtils;
import one.xis.utils.lang.CollectionUtils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.List;

@Data
@Slf4j
@SuperBuilder
abstract class ControllerMethod {

    protected Method method;
    protected String key;
    protected ParameterDeserializer parameterDeserializer;

    Object invoke(ClientRequest request, Object controller) throws Exception {
        return method.invoke(controller, prepareArgs(request));
    }

    @Override
    public String toString() {
        return method.toString();
    }


    protected Object[] prepareArgs(ClientRequest context) throws Exception {
        Object[] args = new Object[method.getParameterCount()];
        var params = method.getParameters();
        for (int i = 0; i < args.length; i++) {
            var param = params[i];
            if (param.isAnnotationPresent(Model.class)) {
                var paramValue = deserializeModelParameter(param, context);
                if (paramValue == null) {
                    paramValue = createModelInstance(param.getType());
                }
                args[i] = paramValue;
            } else if (param.isAnnotationPresent(UserId.class)) {
                args[i] = context.getUserId();
            } else if (param.isAnnotationPresent(ClientId.class)) {
                args[i] = context.getClientId();
            } else if (param.isAnnotationPresent(URLParameter.class)) {
                var key = param.getAnnotation(URLParameter.class).value();
                args[i] = deserializeParameter(context.getUrlParameters().get(key), param);
            } else if (param.isAnnotationPresent(PathVariable.class)) {
                var key = param.getAnnotation(PathVariable.class).value();
                args[i] = deserializeParameter(context.getPathVariables().get(key), param);
            } else if (param.isAnnotationPresent(WidgetParameter.class)) {
                var key = param.getAnnotation(WidgetParameter.class).value();
                args[i] = deserializeParameter(context.getWidgetParameters().get(key), param);
            } else {
                throw new IllegalStateException(method + ": parameter without annotation=" + param);
            }
        }
        return args;
    }

    private Object deserializeModelParameter(Parameter parameter, ClientRequest context) throws IOException {
        var key = parameter.getAnnotation(Model.class).value();
        var paramValue = context.getData().get(key);
        return deserializeParameter(paramValue, parameter);
    }

    private Object deserializeParameter(Parameter parameter, ClientRequest context) throws IOException {
        var key = parameter.getAnnotation(URLParameter.class).value();
        var paramValue = context.getData().get(key);
        return deserializeParameter(paramValue, parameter);
    }

    @SuppressWarnings("unchecked")
    private Object deserializeParameter(Object paramValue, Parameter parameter) throws IOException {
        if (paramValue == null) {
            if (parameter.getType().equals(Iterable.class)) {
                return CollectionUtils.emptyInstance(List.class);
            } else if (Collection.class.isAssignableFrom(parameter.getType())) {
                return CollectionUtils.emptyInstance((Class<Collection<?>>) parameter.getType());
            }
            return null;
        } else if (String.class.isAssignableFrom(parameter.getType())) {
            return paramValue;
        } else if (paramValue instanceof String json) {
            return parameterDeserializer.deserialze(json, parameter);
        } else {
            throw new IllegalArgumentException("paramValue: " + paramValue + " for " + parameter);
        }


    }


    private Object createModelInstance(Class<?> t) {
        try {
            return ClassUtils.newInstance(t);
        } catch (Exception e) {
            log.warn("unable to create instance of " + t, e);
            return null; // we allow this
        }
    }
}
