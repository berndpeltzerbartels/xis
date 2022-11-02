package one.xis.page;

import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.controller.ControllerInvocationService;
import one.xis.dto.ActionRequest;
import one.xis.dto.ActionResponse;
import one.xis.dto.InitialResponse;
import one.xis.dto.ModelRequest;

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
    private final Map<Object, PageMetaData> pageMetaDataMap = new HashMap<>();

    public void addPageController(Object controller) {
        var metaData = pageMetaDataMap.computeIfAbsent(controller, pageMetaDataFactory::createMetaData);
        pageJavascripts.createScript(metaData);
        pageControllers.addController(controller, metaData);
    }

    public InitialResponse invokeInitial(ModelRequest request) {
        var controller = pageControllers.getPageController(request.getControllerId());
        return invocationService.invokeInitial(controller, request);
    }

    public ActionResponse invokeAction(ActionRequest request) {
        var controller = pageControllers.getPageController(request.getControllerId());
        var javascriptClassname = getJavascriptClassname(controller);
        return invocationService.invokeForAction(controller, request, request.getAction(), javascriptClassname);
    }

    private String getJavascriptClassname(Object controller) {
        return pageMetaDataMap.get(controller).getJavascriptClassname();
    }
    
    public PageJavascript getPage(String id) {
        return pageJavascripts.getById(id);
    }


    public Collection<String> getIds() {
        return pageJavascripts.getIds();
    }

    public Map<String, PageJavascript> getPagesByPath() {
        return pageJavascripts.getPagesByPath();
    }

    public PageJavascript getWelcomePageJavascript() {
        return pageJavascripts.getWelcomePage(); // TODO validate there must be exactly one
    }

}
