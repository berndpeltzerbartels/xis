package one.xis.widget;

import lombok.RequiredArgsConstructor;
import one.xis.common.RequestContext;
import one.xis.context.XISComponent;

import java.util.Collection;
import java.util.Map;

@XISComponent
@RequiredArgsConstructor
public class WidgetService {

    private final WidgetJavascripts widgetJavascripts;
    private final WidgetControllers widgetControllers;
    private final WidgetMetaDataFactory widgetMetaDataFactory;

    public void addWidgetConroller(Object controller) {
        WidgetMetaData widgetMetaData = widgetMetaDataFactory.createMetaData(controller);
        widgetJavascripts.add(widgetMetaData);
        widgetControllers.addWidgetController(controller, widgetMetaData);
    }

    public Object invokeInit(RequestContext context) {
        var wrapper = widgetControllers.getWidgetControllerWrapper(context.getJavaClassId());
        return wrapper.invokeInit(context);
    }

    public WidgetJavascript getWidgetJavascript(String id) {
        return widgetJavascripts.getById(id);
    }

    public Collection<String> getIds() {
        return widgetJavascripts.getIds();
    }

    public Map<String, WidgetJavascript> getAllWidgetJavascripts() {
        return widgetJavascripts.getWidgetJavascripts();
    }
}
