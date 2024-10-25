package one.xis.server;

import lombok.Data;
import lombok.NoArgsConstructor;
import one.xis.validation.ValidatorMessages;
import org.tinylog.Logger;

import java.util.Collection;
import java.util.Map;

@Data
@NoArgsConstructor
public class ControllerWrapper {

    /**
     * ID of the component. In case of page, it's an url. For widgets, it's
     * the classes simple name or custom key.
     */
    private String id;

    /**
     * The wrapped instance.
     */
    private Object controller;

    private Collection<ControllerMethod> modelMethods;
    private Map<String, ControllerMethod> actionMethods;
    private Collection<ControllerMethod> formDataMethods;
    private ControllerResultMapper controllerResultMapper;

    void invokeGetModelMethods(ClientRequest request, ControllerResult controllerResult) {
        modelMethods.forEach(method -> invokeForModel(request, controllerResult, method));
    }

    void invokeFormDataMethods(ClientRequest request, ControllerResult controllerResult) {
        formDataMethods.forEach(method -> invokeForFormData(request, controllerResult, method));
    }

    void invokeActionMethod(ClientRequest request, ControllerResult controllerResult) {
        var method = actionMethods.get(request.getAction());
        if (method == null) {
            throw new RuntimeException("No action-method found for action " + request.getAction());
        }
        try {
            var controllerMethodResult = method.invoke(request, controller);
            controllerResultMapper.mapMethodResultToControllerResult(controllerMethodResult, controllerResult);
        } catch (Exception e) {
            Logger.error(e, "Failed to invoke action-method");
            throw new RuntimeException("Failed to invoke action-method: " + method, e);
        }
    }

    Class<?> getControllerClass() {
        return controller.getClass();
    }

    private void invokeForModel(ClientRequest request, ControllerResult controllerResult, ControllerMethod modelMethod) {
        try {
            var controllerMethodResult = modelMethod.invoke(request, controller);
            if (controllerMethodResult.isValidationFailed()) {
                // these validation errors are unexpected, so we throw an exception
                throw exceptionForValidationErrors(controllerMethodResult.getValidatorMessages());
            }
            controllerResultMapper.mapMethodResultToControllerResult(controllerMethodResult, controllerResult);
        } catch (Exception e) {
            Logger.error(e, "Failed to invoke model-method");
            throw new RuntimeException("Failed to invoke model-method " + modelMethod, e);
        }
    }

    private void invokeForFormData(ClientRequest request, ControllerResult controllerResult, ControllerMethod formDataMethod) {
        invokeForModel(request, controllerResult, formDataMethod);
    }

    /**
     * @param validatorMessages
     * @return a RuntimeException with a message containing all validation errors
     */
    private RuntimeException exceptionForValidationErrors(ValidatorMessages validatorMessages) {
        // TODO
        return new RuntimeException("Errors occurred: ");

    }

}
