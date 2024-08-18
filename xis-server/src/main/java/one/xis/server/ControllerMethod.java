package one.xis.server;

import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import one.xis.Action;
import one.xis.Page;
import one.xis.deserialize.InvalidValueError;
import one.xis.deserialize.MainDeserializer;
import one.xis.deserialize.PostProcessingObjects;

import java.lang.reflect.Method;

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
        var postProcessingObjects = new PostProcessingObjects();
        var args = prepareArgs(method, request, postProcessingObjects);
        if (!postProcessingObjects.postProcessingObjects(InvalidValueError.class).isEmpty()) {
            return controllerMethodResultMapper.mapValidationErrorState(request, postProcessingObjects.postProcessingObjects(InvalidValueError.class));
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

    private Object[] prepareArgs(Method method, ClientRequest request, PostProcessingObjects postProcessingObjects) throws Exception {
        var args = new Object[method.getParameterCount()];
        for (var i = 0; i < method.getParameterCount(); i++) {
            args[i] = controllerMethodParameters[i].prepareParameter(request, postProcessingObjects);
        }
        return args;
    }

}
