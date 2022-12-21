package one.xis.page;

import lombok.NonNull;
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
public class PageService {

    private final PageComponents pageComponents;
    private final PageControllers pageControllers;
    private final PageMetaDataFactory pageMetaDataFactory;
    private final ControllerInvocationService invocationService;
    private final Map<Class<?>, PageMetaData> pageMetaDataMap = new HashMap<>();

    public PageComponent addPageController(@NonNull Object controller) {
        var metaData = pageMetaDataMap.computeIfAbsent(controller.getClass(), pageMetaDataFactory::createMetaData);
        var component = pageComponents.createPageComponent(metaData);
        pageControllers.addController(controller, metaData);
        return component;
    }

    public Collection<AjaxResponseMessage> invokeController(InvocationContext invocationContext) {
        var controller = pageControllers.getPageController(invocationContext.getComponentClass());
        return invocationService.invokeController(controller, invocationContext);
    }

    private String getJavascriptClassname(Object controller) {
        return pageMetaDataMap.get(controller.getClass()).getJavascriptClassname();
    }

    public PageComponent getPageComponentByJsClass(String jsClassname) {
        return pageComponents.getByComponentClass(jsClassname);
    }

    public PageComponent getPageComponentByControllerClass(Class<?> controllerClass) {
        return pageComponents.getByControllerClass(controllerClass);
    }

    public PageComponent getPageComponentByPath(String path) {
        return pageComponents.getByPath(path);
    }


    public Collection<String> getClassnames() {
        return pageComponents.getClassnames();
    }

    public Map<String, PageComponent> getPageComponentsByPath() {
        return pageComponents.getPageComponentsByPath();
    }

    public PageComponent getWelcomePageJavascript() {
        return pageComponents.getWelcomePageComponent(); // TODO validate there must be exactly one
    }

}
