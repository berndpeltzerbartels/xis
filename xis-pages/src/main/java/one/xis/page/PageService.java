package one.xis.page;

import lombok.RequiredArgsConstructor;
import one.xis.InvocationResult;
import one.xis.context.XISComponent;
import one.xis.dto.ActionRequest;
import one.xis.dto.ModelRequest;

import java.util.Collection;
import java.util.Map;

@XISComponent
@RequiredArgsConstructor
public class PageService {

    private final PageJavascripts pageJavascripts;
    private final PageControllers pageControllers;
    private final PageMetaDataFactory pageMetaDataFactory;

    public void addPageController(Object controller) {
        var metaData = pageMetaDataFactory.createMetaData(controller);
        pageJavascripts.createScript(metaData);
        pageControllers.addControllerWrapper(controller, metaData);
    }

    public InvocationResult invokeGetModel(ModelRequest request) {
        var wrapper = pageControllers.getPageControllerWrapper(request.getControllerId());
        return wrapper.invokeGetModel(request);
    }

    public Class<?> invokeAction(ActionRequest request) {
        var wrapper = pageControllers.getPageControllerWrapper(request.getControllerId());
        return wrapper.invokeAction(request);
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
