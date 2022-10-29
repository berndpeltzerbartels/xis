package one.xis.controller;

import lombok.Builder;
import one.xis.common.RequestContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Function;

@Builder
public class ControllerWrapper {
    private final Object contoller;
    private final Map<String, Method> methodsByActions;
    private final Map<String, Function<RequestContext, Object[]>> argumentMappers;

    public Object invokeGetModel(RequestContext context) {
        return invoke("GetModel", context);
    }

    public Class<?> invokeAction(RequestContext context) {
        Object actionResult = invoke(context.getAction(), context);
        if (actionResult == null) {
            return null;
        }
        if (actionResult instanceof Class) {
            return (Class<?>) actionResult;
        }
        throw new IllegalStateException();
    }

    public Object invoke(String action, RequestContext context) {
        return invoke(methodsByActions.get(action), context, argumentMappers.get(action));
    }

    private Object invoke(Method method, RequestContext context, Function<RequestContext, Object[]> argumentMapper) {
        try {
            return method.invoke(contoller, argumentMapper.apply(context));
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
