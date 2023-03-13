package one.xis.server;

import lombok.Data;
import lombok.NoArgsConstructor;
import one.xis.resource.Resource;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Data
@NoArgsConstructor
public class ControllerWrapper {

    private String id;
    private Object controller;
    private Resource htmlResource;
    private Class<?> controllerClass;
    private Map<String, ModelMethod> modelMethods;
    private Map<String, ActionMethod> actionMethods;
    private Map<String, ModelTimestampMethod> modelTimestampMethods;

    Map<String, DataItem> invokeGetModelMethods(Request request) {
        var data = new HashMap<String, DataItem>();
        modelMethods.forEach((key, method) -> invokeForDataItem(key, method, request, data));
        return data;
    }

    Object invokeActionMethod(Request request) {
        var method = actionMethods.get(request.getAction());
        return method.invoke(request, controller);
    }

    private void invokeForDataItem(String key, ModelMethod modelMethod, Request request, Map<String, DataItem> result) {
        var timestamp = invokeModelTimestampMethod(key, request);
        var requestTimestamp = request.getData().get(key).getTimestamp();
        if (timestamp.isEmpty() || timestamp.get() > requestTimestamp) {
            result.put(key, new DataItem(modelMethod.invoke(request, controller), timestamp.orElse(System.currentTimeMillis())));
        }
    }

    private Optional<Long> invokeModelTimestampMethod(String key, Request request) {
        return Optional.ofNullable(modelTimestampMethods.get(key))
                .map(modelTimestampMethod -> modelTimestampMethod.invoke(request, controller))
                .filter(Optional::isPresent)
                .map(Optional::get);
    }


}
