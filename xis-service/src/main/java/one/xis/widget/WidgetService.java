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
        widgetJavascripts.add(controller);
        widgetControllers.add(controller);
    }

    public ResourceFile getWidgetJavascript(String id) {
        return widgetJavascripts.get(id);
    }
}
