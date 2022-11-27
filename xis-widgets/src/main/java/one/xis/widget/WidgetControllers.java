package one.xis.widget;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;

import java.util.HashMap;
import java.util.Map;

@XISComponent
@RequiredArgsConstructor
class WidgetControllers {

    private final Map<String, Object> controllers = new HashMap<>();

    void addController(Object widgetController, WidgetMetaData metaData) {
        controllers.put(metaData.getJavascriptClassname(), widgetController);
    }

    Object getWidgetController(@NonNull String className) {
        return controllers.get(className);
    }
}
