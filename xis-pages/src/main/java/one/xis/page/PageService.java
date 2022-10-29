package one.xis.page;

import lombok.RequiredArgsConstructor;
import one.xis.common.RequestContext;
import one.xis.context.XISComponent;

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

    public Object invokeGetModel(RequestContext context) {
        var wrapper = pageControllers.getPageControllerWrapper(context.getJavaClassId());
        return wrapper.invokeGetModel(context);
    }

    public Class<?> invokeAction(RequestContext context) {
        var wrapper = pageControllers.getPageControllerWrapper(context.getJavaClassId());
        return wrapper.invokeAction(context);
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
