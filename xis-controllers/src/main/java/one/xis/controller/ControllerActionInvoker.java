package one.xis.controller;

import lombok.NonNull;
import one.xis.OnAction;
import one.xis.dto.Request;
import one.xis.utils.lang.CollectorUtils;

import java.lang.reflect.Method;
import java.util.Set;

import static one.xis.controller.ControllerUtils.getActionMethods;

class ControllerActionInvoker extends ControllerMethodInvoker {

    private final Set<Method> actionMethodsAvailable;

    ControllerActionInvoker(@NonNull Object controller, @NonNull Request request) {
        super(request, controller);
        actionMethodsAvailable = getActionMethods(controller.getClass());
        this.componentState.putAll(request.getComponentModel());
    }

    Class<?> invokeForAction(@NonNull String action) {
        return (Class<?>) invoke(matchingMethod(action));
    }

    private Method matchingMethod(String action) {
        return actionMethodsAvailable.stream()
                .filter(method -> method.getAnnotation(OnAction.class).value().equals(action))
                .collect(CollectorUtils.toOnlyElement());
    }


}
