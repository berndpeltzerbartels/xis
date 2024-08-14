package one.xis.server;

import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import one.xis.Action;
import one.xis.Page;
import one.xis.deserialize.MainDeserializer;
import one.xis.deserialize.ReportedError;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

@Data
@Slf4j
class ControllerMethod {

    private final Method method;
    private final String key;
    private final MainDeserializer deserializer;
    private final ControllerMethodResultMapper controllerMethodResultMapper;
    private final ControllerMethodParameter[] controllerMethodParameters;

    ControllerMethod(@NonNull Method method, @NonNull String key, @NonNull MainDeserializer deserializer, @NonNull ControllerMethodResultMapper controllerMethodResultMapper) {
        this.method = method;
        this.key = key;
        this.deserializer = deserializer;
        this.controllerMethodResultMapper = controllerMethodResultMapper;
        this.controllerMethodParameters = new ControllerMethodParameter[method.getParameterCount()];
        for (var i = 0; i < method.getParameterCount(); i++) {
            controllerMethodParameters[i] = new ControllerMethodParameter(method, method.getParameters()[i], deserializer);
        }
    }

    @SuppressWarnings("unchecked")
    ControllerMethodResult invoke(@NonNull ClientRequest request, @NonNull Object controller) throws Exception {
        var errors = new ArrayList<ReportedError>();
        var args = prepareArgs(method, request, errors);
        if (!errors.isEmpty()) {
            return controllerMethodResultMapper.mapValidationErrorState(request, errors);
        }
        var returnValue = method.invoke(controller, args);
        if (returnValue == null) { // e.g. a void method
            if (method.isAnnotationPresent(Action.class)) {
                return controllerMethodResultMapper.keepStateOnAction(request, method);
            }
            return controllerMethodResultMapper.keepStateOnAction(request, method);
        }
        if (returnValue instanceof Class clazz && clazz.isAssignableFrom(controller.getClass())) {
            return controllerMethodResultMapper.keepStateOnAction(request, method);
        }
        var controllerMethodResult = controllerMethodResultMapper.mapControllerResult(method, returnValue);
        validateWidgetContainerIdIsPresentIfRequired(controllerMethodResult, method);
        return controllerMethodResult;
    }

    private void validateWidgetContainerIdIsPresentIfRequired(ControllerMethodResult controllerMethodResult, Method method) {
        if (!method.getDeclaringClass().isAnnotationPresent(Page.class)) {
            return;
        }
        if (controllerMethodResult.getNextWidgetId() != null && controllerMethodResult.getWidgetContainerId() == null) {
            // otherwise, we do not know where to show the widget
            throw new IllegalStateException("If a method of page controller returns a widget id, the widget container id must be set.");
        }
    }

    @Override
    public String toString() {
        return "ControllerMethod(" + method.getName() + ")";
    }

    private Object[] prepareArgs(Method method, ClientRequest request, Collection<ReportedError> errors) throws Exception {
        var args = new Object[method.getParameterCount()];
        for (var i = 0; i < method.getParameterCount(); i++) {
            args[i] = controllerMethodParameters[i].prepareParameter(request, errors);
        }
        return args;
    }

}
