package one.xis.server;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Data
@NoArgsConstructor
public class ControllerWrapper {

    private String id;
    private Object controller;
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
        var requestTimestamp = requestTimestamp(request, key);
        var modelTimestamp = invokeModelTimestampMethod(key, request).orElse(null);
        if (modelTimestamp == null || requestTimestamp == null || modelTimestamp > requestTimestamp) {
            var timestamp = modelTimestamp == null ? System.currentTimeMillis() : modelTimestamp;
            result.put(key, new DataItem(modelMethod.invoke(request, controller), timestamp));
        }
    }

    private Long requestTimestamp(Request request, String key) {
        return Optional.ofNullable(request.getData().get(key)).map(DataItem::getTimestamp).orElse(null);
    }

    private Optional<Long> invokeModelTimestampMethod(String key, Request request) {
        return Optional.ofNullable(modelTimestampMethods.get(key))
                .map(modelTimestampMethod -> modelTimestampMethod.invoke(request, controller))
                .filter(Optional::isPresent)
                .map(Optional::get);
    }


}
