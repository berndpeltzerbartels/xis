package one.xis.server;

import lombok.extern.slf4j.Slf4j;
import one.xis.Page;
import one.xis.PageResponse;
import one.xis.Widget;
import one.xis.WidgetResponse;
import one.xis.context.XISInject;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.emptyMap;

@Slf4j
abstract class ControllerService {

    @XISInject
    private DataSerializer dataSerializer;

    @XISInject
    private PageControllerWrappers pageControllerWrappers;

    @XISInject
    private WidgetControllerWrappers widgetControllerWrappers;

    protected ServerResponse invokeGetWidgetModelMethods(int status, ControllerWrapper wrapper, ClientRequest request) {
        return new ServerResponse(status, dataSerializer.serialize(wrapper.invokeGetModelMethods(request)), null, wrapper.getId(), new HashMap<>(), new ValidatorMessages());
    }

    protected ServerResponse invokeGetPageModelMethods(int status, ControllerWrapper wrapper, ClientRequest request) {
        return new ServerResponse(status, dataSerializer.serialize(wrapper.invokeGetModelMethods(request)), wrapper.getId(), null, new HashMap<>(), new ValidatorMessages());
    }

    protected ControllerWrapper widgetControllerWrapperByClass(Class<?> controllerClass) {
        return widgetControllerWrappers.findByClass(controllerClass)
                .orElseThrow(() -> new IllegalStateException("not a widget-controller:" + controllerClass));
    }

    protected ControllerWrapper widgetControllerWrapperById(String id) {
        return widgetControllerWrappers.findWidgetById(id)
                .orElseThrow(() -> new IllegalStateException("not a widget-controller:" + id));
    }

    protected ServerResponse createPageResponse(Map<String, Object> modelData, ControllerWrapper pageControllerWrapper) {
        return new ServerResponse(200, dataSerializer.serialize(modelData), pageControllerWrapper.getId(), null, emptyMap(), new ValidatorMessages());
    }

    protected ServerResponse createWidgetResponse(Map<String, Object> modelData, ControllerWrapper widgetControllerWrapper) {
        return new ServerResponse(200, dataSerializer.serialize(modelData), null, widgetControllerWrapper.getId(), emptyMap(), new ValidatorMessages());
    }

    protected ServerResponse processActionResult(ClientRequest request, PageResponse pageResponse) {
        var controllerClass = pageResponse.getControllerClass();
        var controllerWrapper = widgetControllerWrapperByClass(controllerClass);
        request.getUrlParameters().putAll(pageResponse.getUrlParameters()); // TODO May be better to create a context class to avoid mutating the request
        request.getPathVariables().putAll(pageResponse.getPathVariables());
        return invokeGetWidgetModelMethods(200, controllerWrapper, request);
    }

    protected ServerResponse processActionResult(ClientRequest request, WidgetResponse widgetResponse) {
        var controllerClass = widgetResponse.getControllerClass();
        var controllerWrapper = widgetControllerWrapperByClass(controllerClass);
        request.getWidgetParameters().putAll(widgetResponse.getWidgetParameters()); // TODO May be better to create a context class to avoid mutating the request
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

    protected ServerResponse processPageResult(ClientRequest request, PageResponse pageResponse) {
        var controllerClass = pageResponse.getControllerClass();
        var controllerWrapper = pageControllerWrapperByClass(controllerClass);
        request.getPathVariables().putAll(pageResponse.getPathVariables());
        request.getUrlParameters().putAll(pageResponse.getUrlParameters());
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
