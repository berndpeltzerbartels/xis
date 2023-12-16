package one.xis.server;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.xis.PathVariable;
import one.xis.*;
import one.xis.context.XISComponent;
import one.xis.utils.lang.ClassUtils;
import one.xis.utils.lang.CollectionUtils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.List;

@Slf4j
@XISComponent
@RequiredArgsConstructor
class ControllerMethodParameterFactory {
    protected final ParameterDeserializer parameterDeserializer;

    Object[] prepareArgs(Method method, ClientRequest context) throws Exception {
        Object[] args = new Object[method.getParameterCount()];
        var params = method.getParameters();
        for (int i = 0; i < args.length; i++) {
            var param = params[i];
            if (param.isAnnotationPresent(ModelData.class)) {
                args[i] = deserializeModelParameter(param, context);
            } else if (param.isAnnotationPresent(FormData.class)) {
                args[i] = deserializeFormDataParameter(param, context);
            } else if (param.isAnnotationPresent(UserId.class)) {
                args[i] = context.getUserId();
            } else if (param.isAnnotationPresent(ClientId.class)) {
                args[i] = context.getClientId();
            } else if (param.isAnnotationPresent(URLParameter.class)) {
                var key = param.getAnnotation(URLParameter.class).value();
                args[i] = deserializeParameter(context.getUrlParameters().get(key), param);
            } else if (param.isAnnotationPresent(one.xis.PathVariable.class)) {
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
        var key = parameter.getAnnotation(ModelData.class).value();
        var paramValue = context.getData().get(key);
        return deserializeParameter(paramValue, parameter);
    }

    private Object deserializeFormDataParameter(Parameter parameter, ClientRequest context) throws IOException {
        var key = parameter.getAnnotation(FormData.class).value();
        var paramValue = context.getFormData().get(key);
        return deserializeParameter(paramValue, parameter);
    }


    @SuppressWarnings("unchecked")
    private Object deserializeParameter(String paramValue, Parameter parameter) throws IOException {
        if (paramValue == null) {
            if (parameter.getType().equals(Iterable.class)) {
                return CollectionUtils.emptyInstance(List.class);
            } else if (Collection.class.isAssignableFrom(parameter.getType())) {
                return CollectionUtils.emptyInstance((Class<Collection<?>>) parameter.getType());
            }
            return null;
        } else if (String.class.isAssignableFrom(parameter.getType())) {
            return paramValue;
        }
        return parameterDeserializer.deserialze(paramValue, parameter);
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
