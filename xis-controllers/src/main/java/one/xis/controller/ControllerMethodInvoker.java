package one.xis.controller;

import lombok.Getter;
import one.xis.*;
import one.xis.dto.Request;
import one.xis.utils.lang.ClassUtils;
import one.xis.utils.lang.MethodUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

abstract class ControllerMethodInvoker {

    protected final Request request;
    protected final Object controller;


    @Getter
    protected final Map<String, Object> clientState = new HashMap<>();

    @Getter
    protected final Map<String, Object> componentModel = new HashMap<>();

    protected ControllerMethodInvoker(Request request, Object controller) {
        this.request = request;
        this.controller = controller;
        this.clientState.putAll(request.getClientState());
    }

    // TODO COnvert primitives String etc
    protected Object paramValue(Parameter parameter) {
        if (parameter.isAnnotationPresent(State.class)) {
            String key = getStateKey(parameter);
            return clientState.containsKey(key) ? clientState.get(key) : ClassUtils.newInstance(parameter.getType());
        }
        if (parameter.isAnnotationPresent(Model.class)) {
            return componentModel.get(getModelKey(parameter));
        }
        if (parameter.isAnnotationPresent(UserId.class)) {
            return null; // TODO
        }
        if (parameter.isAnnotationPresent(Token.class)) {
            return request.getToken();
        }
        if (parameter.isAnnotationPresent(ClientId.class)) {
            return request.getClientId();
        }
        // TODO Widget/Page-Parameters
        return null;
    }


    protected Object invoke(Method method) {
        var args = prepareArgs(method);
        var rv = MethodUtils.invoke(controller, method, args);
        for (int i = 0; i < args.length; i++) {
            Parameter parameter = method.getParameters()[i];
            if (parameter.isAnnotationPresent(State.class)) {
                clientState.put(getModelKey(parameter), args[i]);
            }
            if (parameter.isAnnotationPresent(Model.class)) {
                componentModel.put(getModelKey(parameter), args[i]);
            }
        }
        return rv;
    }


    private Object[] prepareArgs(Method method) {
        var rv = new Object[method.getParameters().length];
        for (int i = 0; i < rv.length; i++) {
            rv[i] = paramValue(method.getParameters()[i]);
        }
        return rv;
    }


    protected String getStateKey(Parameter parameter) {
        var annotation = parameter.getAnnotation(State.class);
        return annotation.value().isEmpty() ? parameter.getName() : annotation.value();
    }

    protected String getModelKey(Parameter parameter) {
        var annotation = parameter.getAnnotation(Model.class);
        return annotation.value().isEmpty() ? parameter.getName() : annotation.value();
    }

    protected String getStateKey(java.lang.reflect.Method method) {
        var annotation = method.getAnnotation(State.class);
        return annotation.value().isEmpty() ? method.getName() : annotation.value();
    }

    protected String getModelKey(Method method) {
        var annotation = method.getAnnotation(Model.class);
        return annotation.value().isEmpty() ? method.getName() : annotation.value();
    }
}
