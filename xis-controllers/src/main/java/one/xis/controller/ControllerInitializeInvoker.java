package one.xis.controller;


import lombok.Getter;
import one.xis.Model;
import one.xis.State;
import one.xis.dto.Request;
import one.xis.utils.lang.MethodUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
class ControllerInitializeInvoker extends ControllerMethodInvoker {

    private final Set<Method> initialMethodsAvailable;

    ControllerInitializeInvoker(Object controller, Request request) {
        super(request, controller);
        this.initialMethodsAvailable = MethodUtils.methods(controller).stream()
                .filter(method -> method.isAnnotationPresent(Model.class))
                .collect(Collectors.toSet());
    }

    void invokeInitial() {
        Method method;
        while ((method = nextMethod()) != null) {
            Object returnValue = invoke(method);
            if (method.isAnnotationPresent(Model.class)) {
                componentModel.put(getModelKey(method), returnValue);
            }
            if (method.isAnnotationPresent(State.class)) {
                clientState.put(getStateKey(method), returnValue);
            }
        }
        if (!initialMethodsAvailable.isEmpty()) {
            throw new IllegalStateException(controller.getClass() + " has circular parameters");
        }
    }

    private Method nextMethod() {
        Method rv = null;
        for (Method method : initialMethodsAvailable) {
            if (isInvocable(method)) {
                rv = method;
            }
        }
        initialMethodsAvailable.remove(rv);
        return rv;
    }

    private boolean isInvocable(Method method) {
        for (Parameter parameter : method.getParameters()) {
            if (parameter.isAnnotationPresent(Model.class)) {
                if (!componentModel.containsKey(getModelKey(method))) {
                    return false;
                }
            }
        }
        return true;
    }
}
