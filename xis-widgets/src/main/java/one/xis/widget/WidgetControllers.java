package one.xis.widget;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;

import java.util.HashMap;
import java.util.Map;

@XISComponent
@RequiredArgsConstructor
class WidgetControllers {
    
    private final Map<String, Object> controllerWrappers = new HashMap<>();

    void addController(Object widgetController, WidgetMetaData metaData) {
        controllerWrappers.put(metaData.getId(), widgetController);
    }

    Object getWidgetController(@NonNull String id) {
        return controllerWrappers.get(id);
    }
}
