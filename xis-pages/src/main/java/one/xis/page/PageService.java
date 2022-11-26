package one.xis.page;

import lombok.NonNull;
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
public class PageService {

    private final PageJavascripts pageJavascripts;
    private final PageControllers pageControllers;
    private final PageMetaDataFactory pageMetaDataFactory;
    private final ControllerInvocationService invocationService;
    private final Map<Class<?>, PageMetaData> pageMetaDataMap = new HashMap<>();

    public void addPageController(@NonNull Object controller) {
        var metaData = pageMetaDataMap.computeIfAbsent(controller.getClass(), pageMetaDataFactory::createMetaData);
        pageJavascripts.createScript(metaData);
        pageControllers.addController(controller, metaData);
    }

    public InitialResponse invokeInitial(InitialRequest request) {
        var controller = pageControllers.getPageController(request.getControllerClass());
        return invocationService.invokeInitial(controller, request);
    }

    public ActionResponse invokeAction(ActionRequest request) {
        var controller = pageControllers.getPageController(request.getControllerClass());
        var javascriptClassname = getJavascriptClassname(controller);
        return invocationService.invokeForAction(controller, request, request.getAction(), javascriptClassname);
    }
    

    private String getJavascriptClassname(Object controller) {
        return pageMetaDataMap.get(controller.getClass()).getJavascriptClassname();
    }

    public PageJavascript getPage(String id) {
        return pageJavascripts.getByControllerClass(id);
    }


    public Collection<String> getClassnames() {
        return pageJavascripts.getClassnames();
    }

    public Map<String, PageJavascript> getPagesByPath() {
        return pageJavascripts.getPagesByPath();
    }

    public PageJavascript getWelcomePageJavascript() {
        return pageJavascripts.getWelcomePage(); // TODO validate there must be exactly one
    }

}
