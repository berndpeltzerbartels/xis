package one.xis.server;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import one.xis.Action;
import one.xis.Page;
import one.xis.validation.Validation;
import one.xis.validation.ValidationErrors;

import java.lang.reflect.Method;

@Data
@Slf4j
@SuperBuilder
class ControllerMethod {

    protected Method method;
    protected String key;
    protected ParameterPreparer parameterPreparer;
    protected Validation validation;
    protected ControllerMethodResultMapper controllerMethodResultMapper;

    ControllerMethodResult invoke(@NonNull ClientRequest request, @NonNull Object controller) throws Exception {
        var errors = new ValidationErrors();
        var args = prepareArgs(method, request, errors);
        validateArgs(args, method, request, errors);
        if (errors.hasErrors()) {
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

    private Object[] prepareArgs(Method method, ClientRequest request, ValidationErrors errors) throws Exception {
        return parameterPreparer.prepareParameters(method, request, errors);
    }


    private void validateArgs(@NonNull Object[] args, @NonNull Method method, @NonNull ClientRequest request, ValidationErrors errors) {
        for (var i = 0; i < args.length; i++) {
            var parameter = method.getParameters()[i];
            var parameterValue = args[i];
            validation.validate(parameter, parameterValue, errors);
        }
    }
}
