package one.xis.server;

import lombok.extern.slf4j.Slf4j;
import one.xis.Page;
import one.xis.PageResult;
import one.xis.Widget;
import one.xis.WidgetResult;
import one.xis.context.XISInject;

import java.util.HashMap;

@Slf4j
abstract class ControllerService {

    @XISInject
    private DataSerializer dataSerializer;

    @XISInject
    private PageControllerWrappers pageControllerWrappers;

    @XISInject
    private WidgetControllerWrappers widgetControllerWrappers;

    protected ServerResponse invokeGetWidgetModelMethods(int status, ControllerWrapper wrapper, ClientRequest request) {
        return new ServerResponse(status, dataSerializer.serialize(wrapper.invokeGetModelMethods(request)), null, wrapper.getId(), new HashMap<>());
    }

    protected ServerResponse invokeGetPageModelMethods(int status, ControllerWrapper wrapper, ClientRequest request) {
        return new ServerResponse(status, dataSerializer.serialize(wrapper.invokeGetModelMethods(request)), wrapper.getId(), null, new HashMap<>());
    }

    protected ControllerWrapper widgetControllerWrapperByClass(Class<?> controllerClass) {
        return widgetControllerWrappers.findByClass(controllerClass)
                .orElseThrow(() -> new IllegalStateException("not a widget-controller:" + controllerClass));
    }

    protected ControllerWrapper widgetControllerWrapperById(String id) {
        return widgetControllerWrappers.findWidgetById(id)
                .orElseThrow(() -> new IllegalStateException("not a widget-controller:" + id));
    }

    protected ServerResponse processActionResult(ClientRequest request, PageResult pageResult) {
        var controllerClass = pageResult.getControllerClass();
        var controllerWrapper = widgetControllerWrapperByClass(controllerClass);
        request.getUrlParameters().putAll(pageResult.getUrlParameters()); // TODO May be better to create a context class to avoid mutating the request
        request.getPathVariables().putAll(pageResult.getPathVariables());
        return invokeGetWidgetModelMethods(200, controllerWrapper, request);
    }

    protected ServerResponse processActionResult(ClientRequest request, WidgetResult widgetResult) {
        var controllerClass = widgetResult.getControllerClass();
        var controllerWrapper = widgetControllerWrapperByClass(controllerClass);
        request.getWidgetParameters().putAll(widgetResult.getWidgetParameters()); // TODO May be better to create a context class to avoid mutating the request
        return invokeGetWidgetModelMethods(200, controllerWrapper, request);
    }

    protected ServerResponse processActionResult(ClientRequest request, Class<?> controllerClass) {
        if (controllerClass.isAnnotationPresent(Page.class)) {
            var controllerWrapper = pageControllerWrapperByClass(controllerClass);
            return invokeGetPageModelMethods(200, controllerWrapper, request);
        } else if (controllerClass.isAnnotationPresent(Widget.class)) {
            var controllerWrapper = widgetControllerWrapperByClass(controllerClass);
            return invokeGetWidgetModelMethods(200, controllerWrapper, request);
        } else {
            throw new IllegalStateException("returned type is not a controller class: " + controllerClass);
        }
    }

    protected ServerResponse processPageResult(ClientRequest request, PageResult pageResult) {
        var controllerClass = pageResult.getControllerClass();
        var controllerWrapper = widgetControllerWrapperByClass(controllerClass);
        request.getPathVariables().putAll(pageResult.getPathVariables());
        request.getUrlParameters().putAll(pageResult.getUrlParameters());
        return invokeGetPageModelMethods(200, controllerWrapper, request);
    }

    protected ControllerWrapper pageControllerWrapperByClass(Class<?> controllerClass) {
        return pageControllerWrappers.findByClass(controllerClass)
                .orElseThrow(() -> new IllegalStateException("not a page-controller:" + controllerClass));
    }

    protected ControllerWrapper pageControllerWrapperById(String id) {
        return pageControllerWrappers.findByPath(id)
                .orElseThrow(() -> new IllegalStateException("not a page-controller:" + id));
    }

}
