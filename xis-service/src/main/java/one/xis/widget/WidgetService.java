package one.xis.widget;

import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.resource.ResourceFile;

@XISComponent
@RequiredArgsConstructor
public class WidgetService {
    // TODO remove all "public" ?
    private final WidgetJavascripts widgetJavascripts;
    private final WidgetControllers widgetControllers;

    public void addWidgetConroller(Object controller) {
        String key = createKey(controller);
        widgetJavascripts.add(key, controller);
        widgetControllers.add(key, controller);
    }

    public ResourceFile getWidgetJavascript(String id) {
        return widgetJavascripts.get(id);
    }


    private String createKey(Object controller) {
        var controllerClass = controller.getClass();
        String alias = controllerClass.getAnnotation(one.xis.Widget.class).value();
        return alias.isEmpty() ? controllerClass.getSimpleName() : alias;
    }

}
