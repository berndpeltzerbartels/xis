package one.xis.controller;

import one.xis.OnAction;
import one.xis.dto.Request;
import one.xis.utils.lang.CollectorUtils;
import one.xis.utils.lang.MethodUtils;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.stream.Collectors;

class ControllerActionInvoker extends ControllerMethodInvoker {

    private final Set<Method> actionMethodsAvailable;

    ControllerActionInvoker(Object controller, Request request) {
        super(request, controller);
        actionMethodsAvailable = MethodUtils.methods(controller).stream()
                .filter(method -> method.isAnnotationPresent(OnAction.class))
                .collect(Collectors.toSet());
        this.componentModel.putAll(request.getComponentModel());
    }
    
    Class<?> invokeForAction(String action) {
        return (Class<?>) invoke(matchingMethod(action));
    }

    private Method matchingMethod(String action) {
        return actionMethodsAvailable.stream()
                .filter(method -> method.getAnnotation(OnAction.class).value().equals(action))
                .collect(CollectorUtils.toOnlyElement());
    }


}
