package one.xis.controller;

import lombok.NonNull;
import one.xis.OnAction;
import one.xis.dto.ActionRequest;
import one.xis.dto.InitialRequest;
import one.xis.utils.lang.CollectorUtils;

import java.lang.reflect.Method;
import java.util.Set;

import static one.xis.controller.ControllerUtils.getActionMethods;

class ControllerActionInvoker extends ControllerMethodInvoker {

    private final Set<Method> actionMethodsAvailable;

    ControllerActionInvoker(@NonNull Object controller, @NonNull ActionRequest request) {
        super(request, controller);
        actionMethodsAvailable = getActionMethods(controller.getClass());
        this.componentState.putAll(request.getComponentModel());
    }

    ControllerActionInvoker(@NonNull Object controller, @NonNull InitialRequest request) {
        super(request, controller);
        actionMethodsAvailable = getActionMethods(controller.getClass());
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
