package one.xis.server;

import lombok.Data;
import lombok.NoArgsConstructor;
import one.xis.validation.ValidatorMessages;
import org.tinylog.Logger;

import java.util.Map;
import java.util.stream.Collectors;

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

    private Map<String, ControllerMethod> modelMethods;
    private Map<String, ControllerMethod> actionMethods;

    void invokeGetModelMethods(ClientRequest request, ControllerResult controllerResult) {
        modelMethods.forEach((key, method) -> invokeForModel(key, request, controllerResult, method));
    }

    void invokeActionMethod(ClientRequest request, ControllerResult controllerResult) {
        var method = actionMethods.get(request.getAction());
        if (method == null) {
            throw new RuntimeException("No action-method found for action " + request.getAction());
        }
        try {
            var controllerMethodResult = method.invoke(request, controller);
            controllerResult.setNextPageURL(controllerMethodResult.getNextPageURL());
            controllerResult.setNextWidgetId(controllerMethodResult.getNextWidgetId());
            controllerResult.setWidgetContainerId(controllerMethodResult.getWidgetContainerId());
            controllerResult.getWidgetParameters().putAll(controllerMethodResult.getWidgetParameters());
            controllerResult.getPathVariables().putAll(controllerMethodResult.getPathVariables());
            controllerResult.getUrlParameters().putAll(controllerMethodResult.getUrlParameters());
            controllerResult.getValidatorMessages().getGlobalMessages().addAll(controllerMethodResult.getValidatorMessages().getGlobalMessages());
            controllerResult.getValidatorMessages().getMessages().putAll(controllerMethodResult.getValidatorMessages().getMessages());
            if (controllerMethodResult.isValidationFailed()) {
                controllerResult.setValidationFailed(true);
            }
        } catch (Exception e) {
            Logger.error(e, "Failed to invoke action-method");
            throw new RuntimeException("Failed to invoke action-method: " + method, e);
        }
    }

    Class<?> getControllerClass() {
        return controller.getClass();
    }

    private void invokeForModel(String dataKey, ClientRequest request, ControllerResult controllerResult, ControllerMethod modelMethod) {
        try {
            var controllerMethodResult = modelMethod.invoke(request, controller);
            if (controllerMethodResult.isValidationFailed()) {
                // these validation errors are unexpected, so we throw an exception
                throw exceptionForValiationErrors(controllerMethodResult.getValidatorMessages());
            }
            controllerResult.getModelData().putAll(controllerMethodResult.getModelData());
        } catch (Exception e) {
            Logger.error(e, "Failed to invoke model-method");
            throw new RuntimeException("Failed to invoke model-method " + modelMethod, e);
        }
    }

    /**
     * @param validatorMessages
     * @return a RuntimeException with a message containing all validation errors
     */
    private RuntimeException exceptionForValiationErrors(ValidatorMessages validatorMessages) {
        var message = validatorMessages.getMessages().keySet().stream().map(key -> key + ": " + validatorMessages.getMessages().get(key)).collect(Collectors.joining(", "));
        return new RuntimeException("Errors occurred: " + message);

    }


}
