package one.xis.server;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

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
    private ComponentAttributes componentAttributes;

    Map<String, Object> invokeGetModelMethods(Request request) {
        var data = new HashMap<String, Object>();
        modelMethods.forEach((key, method) -> invokeForModel(key, method, request, data));
        return data;
    }

    Object invokeActionMethod(Request request) {
        var method = actionMethods.get(request.getAction());
        return method.invoke(request, controller);
    }

    Class<?> getControllerClass() {
        return controller.getClass();
    }

    private void invokeForModel(String key, ModelMethod modelMethod, Request request, Map<String, Object> result) {
        result.put(key, modelMethod.invoke(request, controller));
    }


}
