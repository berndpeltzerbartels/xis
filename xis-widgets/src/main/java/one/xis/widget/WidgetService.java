package one.xis.widget;

import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.controller.ControllerInvocationService;
import one.xis.dto.ActionRequest;
import one.xis.dto.ActionResponse;
import one.xis.dto.InitialRequest;
import one.xis.dto.InitialResponse;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@XISComponent
@RequiredArgsConstructor
public class WidgetService {

    private final WidgetJavascripts widgetJavascripts;
    private final WidgetControllers widgetControllers;
    private final WidgetMetaDataFactory widgetMetaDataFactory;
    private final ControllerInvocationService invocationService;
    private final Map<Object, WidgetMetaData> widgetMetaDataMap = new HashMap<>();

    public void t(Object controller) {
        var widgetMetaData = widgetMetaDataMap.computeIfAbsent(controller, widgetMetaDataFactory::createMetaData);
        widgetJavascripts.add(widgetMetaData);
        widgetControllers.addController(controller, widgetMetaData);
    }

    public InitialResponse invokeInitial(InitialRequest request) {
        var controller = widgetControllers.getWidgetController(request.getControllerClass());
        return invocationService.invokeInitial(controller, request);
    }

    public ActionResponse invokeAction(ActionRequest request) {
        var controller = widgetControllers.getWidgetController(request.getControllerClass());
        var javascriptClassname = getJavascriptClassname(controller);
        return invocationService.invokeForAction(controller, request, request.getAction(), javascriptClassname);
    }

    public WidgetJavascript getWidgetJavascript(String widgetKey) {
        return widgetJavascripts.getByControllerClass(widgetKey);
    }

    public Collection<String> getIds() {
        return widgetJavascripts.getIds();
    }

    public Map<String, WidgetJavascript> getAllWidgetJavascripts() {
        return widgetJavascripts.getWidgetJavascripts();
    }


    private String getJavascriptClassname(Object controller) {
        return widgetMetaDataMap.get(controller).getJavascriptClassname();
    }
}
