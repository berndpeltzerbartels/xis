package one.xis.server;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class ControllerWrapper {

    /**
     * ID of the component. In case of page, it's an url. For widgets it's
     * the classes simple name or custom key.
     */
    private String id;

    /**
     * The wrapped instance.
     */
    private Object controller;

    private Map<String, ModelMethod> modelMethods;
    private Map<String, ActionMethod> actionMethods;

    Map<String, Object> invokeGetModelMethods(ClientRequest request) {
        var data = new HashMap<String, Object>();
        var errors = new HashMap<String, ValidationError>();
        modelMethods.forEach((key, method) -> invokeForModel(key, method, request, data, errors));
        if (!errors.isEmpty()) {
            throw exceptionForErrors(errors);
        }
        return data;
    }

    ControllerMethodResult invokeActionMethod(ClientRequest request) {
        @NonNull var method = actionMethods.get(request.getAction());
        try {
            return method.invoke(request, controller);
        } catch (Exception e) {
            Logger.error(e, "Failed to invoke action-method");
            throw new RuntimeException("Failed to invoke action-method: " + method, e);
        }
    }

    Class<?> getControllerClass() {
        return controller.getClass();
    }

    private void invokeForModel(String key, ModelMethod modelMethod, ClientRequest request, Map<String, Object> result, Map<String, ValidationError> errors) {
        try {
            var methodResult = modelMethod.invoke(request, controller);
            result.put(key, methodResult.returnValue());
            errors.putAll(methodResult.errors());
        } catch (Exception e) {
            Logger.error(e, "Failed to invoke model-method");
            throw new RuntimeException("Failed to invoke model-method " + modelMethod, e);
        }
    }

    private RuntimeException exceptionForErrors(Map<String, ValidationError> errors) {
        var message = errors.entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue())
                .collect(Collectors.joining(", "));
        return new RuntimeException("Errors occurred: " + message);

    }


}
