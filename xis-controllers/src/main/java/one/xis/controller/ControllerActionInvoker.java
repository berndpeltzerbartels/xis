package one.xis.controller;

import one.xis.OnAction;
import one.xis.dto.Request;
import one.xis.utils.lang.MethodUtils;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class ControllerActionInvoker extends ControllerMethodInvoker {

    private final Set<Method> actionMethodsAvailable;

    ControllerActionInvoker(Object controller, Request request) {
        super(request, controller);
        actionMethodsAvailable = MethodUtils.methods(controller).stream()
                .filter(method -> method.isAnnotationPresent(OnAction.class))
                .collect(Collectors.toSet());
    }

    void invokeForAction(String action) {
        matchingMethods(action).forEach(this::invoke);
    }

    private Stream<Method> matchingMethods(String action) {
        return actionMethodsAvailable.stream()
                .filter(method -> method.getAnnotation(OnAction.class).value().equals(action));
    }


}
