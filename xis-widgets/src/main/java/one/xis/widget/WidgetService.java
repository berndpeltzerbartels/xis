package one.xis.widget;

import lombok.RequiredArgsConstructor;
import one.xis.ajax.AjaxResponseMessage;
import one.xis.ajax.InvocationContext;
import one.xis.context.XISComponent;
import one.xis.controller.ControllerInvocationService;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@XISComponent
@RequiredArgsConstructor
public class WidgetService {

    private final WidgetComponents widgetComponents;
    private final WidgetControllers widgetControllers;
    private final WidgetMetaDataFactory widgetMetaDataFactory;
    private final ControllerInvocationService invocationService;
    private final Map<Object, WidgetMetaData> widgetMetaDataMap = new HashMap<>();

    public void addWidgetConroller(Object controller) {
        var widgetMetaData = widgetMetaDataMap.computeIfAbsent(controller, widgetMetaDataFactory::createMetaData);
        widgetComponents.add(widgetMetaData);
        widgetControllers.addController(controller, widgetMetaData);
    }

    public Collection<AjaxResponseMessage> invokeController(InvocationContext invocationContext) {
        var controller = widgetControllers.getWidgetController(invocationContext.getComponentClass());
        return invocationService.invokeController(controller, invocationContext);
    }

    public WidgetComponent getWidgetComponent(String jsClassname) {
        return widgetComponents.getByComponentClass(jsClassname);
    }

    public Collection<String> getClassnames() {
        return widgetComponents.getJsClassnames();
    }

    public Map<String, WidgetComponent> getAllWidgetJavascripts() {
        return widgetComponents.getWidgetJavascripts();
    }


    private String getJavascriptClassname(Object controller) {
        return widgetMetaDataMap.get(controller).getJavascriptClassname();
    }
}
