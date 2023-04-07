package one.xis.server;

import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.SuperBuilder;
import one.xis.ClientId;
import one.xis.Model;
import one.xis.UserId;
import one.xis.utils.lang.CollectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.List;

@Data
@SuperBuilder
abstract class ControllerMethod {

    protected Method method;
    protected String key;
    protected ParameterDeserializer parameterDeserializer;

    @SneakyThrows
    Object invoke(Request request, Object controller) {
        return method.invoke(controller, prepareArgs(request));
    }

    @Override
    public String toString() {
        return method.toString();
    }


    protected Object[] prepareArgs(Request context) {
        Object[] args = new Object[method.getParameterCount()];
        var params = method.getParameters();
        for (int i = 0; i < args.length; i++) {
            var param = params[i];
            if (param.isAnnotationPresent(Model.class)) {
                args[i] = modelParameter(param, context);
            } else if (param.isAnnotationPresent(UserId.class)) {
                args[i] = context.getUserId();
            } else if (param.isAnnotationPresent(ClientId.class)) {
                args[i] = context.getClientId();
            } else {
                throw new IllegalStateException("no annotation: " + param);
            }
        }
        return args;
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    private Object modelParameter(Parameter parameter, Request context) {
        var key = parameter.getAnnotation(Model.class).value();
        var paramValue = context.getData().get(key);
        if (paramValue == null) {
            if (parameter.getType().equals(Iterable.class)) {
                return CollectionUtils.emptyInstance(List.class);
            } else if (Collection.class.isAssignableFrom(parameter.getType())) {
                return CollectionUtils.emptyInstance((Class<Collection<?>>) parameter.getType());
            }
        }
        if (paramValue instanceof String) {
            if (parameter.getType() == String.class) {
                return paramValue;
            }
            return parameterDeserializer.deserialze((String) paramValue, parameter);
        }
        return paramValue;
    }


}
