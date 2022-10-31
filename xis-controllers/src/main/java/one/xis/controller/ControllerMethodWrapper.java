package one.xis.controller;

import lombok.Data;
import one.xis.*;
import one.xis.dto.Request;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;

@Data
class ControllerMethodWrapper {
    private final Object controller;
    private final Method method;
    private final List<MethodParameterFactory> methodParameterFactories;

    InvocationResult invoke(Request request) {
        Object[] args = prepareArgs(request);
        try {
            Object returnValue = method.invoke(controller, args);
            return invocationResult(returnValue, args);

        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private InvocationResult invocationResult(Object returnValue, Object[] args) {
        var result = new InvocationResult();
        result.setReturnValue(returnValue);
        for (int i = 0; i < args.length; i++) {
            Parameter parameter = method.getParameters()[i];
            if (parameter.isAnnotationPresent(State.class)) {
                State annotation = parameter.getAnnotation(State.class);
                result.addClientState(getKey(parameter, annotation), args[i]);
            }
            if (parameter.isAnnotationPresent(Model.class)) {
                Model annotation = parameter.getAnnotation(Model.class);
                result.addModel(getKey(parameter, annotation), args[i]);
            }
        }
        return result;
    }

    private Object[] prepareArgs(Request request) {
        Object[] rv = new Object[method.getParameters().length];
        for (int i = 0; i < rv.length; i++) {
            rv[i] = paramValue(method.getParameters()[i], request);
        }
        return rv;
    }

    private Object paramValue(Parameter parameter, Request request) {
        if (parameter.isAnnotationPresent(State.class)) {
            return request.getClientState().get(getKey(parameter, parameter.getAnnotation(State.class)));
        }
        if (parameter.isAnnotationPresent(Model.class)) {
            return request.getClientState().get(getKey(parameter, parameter.getAnnotation(Model.class)));
        }
        if (parameter.isAnnotationPresent(UserId.class)) {
            return null;
        }
        if (parameter.isAnnotationPresent(Token.class)) {
            return request.getToken();
        }
        if (parameter.isAnnotationPresent(ClientId.class)) {
            return request.getClientId();
        }
        return null;
    }

    private String getKey(Parameter parameter, State annotation) {
        return annotation.value().isEmpty() ? parameter.getName() : annotation.value();
    }

    private String getKey(Parameter parameter, Model annotation) {
        return annotation.value().isEmpty() ? parameter.getName() : annotation.value();
    }
}
