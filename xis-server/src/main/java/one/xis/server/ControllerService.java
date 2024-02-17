package one.xis.server;

import lombok.extern.slf4j.Slf4j;
import one.xis.Page;
import one.xis.PageResponse;
import one.xis.Widget;
import one.xis.WidgetResponse;
import one.xis.context.XISInject;
import one.xis.validation.ValidatorMessages;

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

    protected void invokeGetWidgetModelMethods(ControllerWrapper wrapper, ClientRequest request, ServerResponse response) {
        response.setData(dataSerializer.serialize(wrapper.invokeGetModelMethods(request)));
        response.setNextWidgetId(wrapper.getId());
        response.setWidgetParameters(emptyMap());
        response.setValidatorMessages(new ValidatorMessages());
    }

    protected void invokeGetPageModelMethods(ControllerWrapper wrapper, ClientRequest request, ServerResponse response) {
        response.setData(dataSerializer.serialize(wrapper.invokeGetModelMethods(request)));
        response.setNextPageURL(wrapper.getId());
        response.setWidgetParameters(emptyMap());
        response.setValidatorMessages(new ValidatorMessages());
    }

    protected ControllerWrapper widgetControllerWrapperByClass(Class<?> controllerClass) {
        return widgetControllerWrappers.findByClass(controllerClass)
                .orElseThrow(() -> new IllegalStateException("not a widget-controller:" + controllerClass));
    }

    protected ControllerWrapper widgetControllerWrapperById(String id) {
        return widgetControllerWrappers.findWidgetById(id)
                .orElseThrow(() -> new IllegalStateException("not a widget-controller:" + id));
    }

    protected void processPageResult(ServerResponse response, Map<String, Object> modelData, ControllerWrapper pageControllerWrapper) {
        response.setHttpStatus(200);
        response.setData(dataSerializer.serialize(modelData));
        response.setNextPageURL(pageControllerWrapper.getId());
        response.setWidgetParameters(emptyMap());
        response.setValidatorMessages(new ValidatorMessages());
    }

    protected void updateWidgetResponse(ServerResponse response, Map<String, Object> modelData, ControllerWrapper widgetControllerWrapper) {
        response.setHttpStatus(200);
        response.setData(dataSerializer.serialize(modelData));
        response.setNextWidgetId(widgetControllerWrapper.getId());
        response.setWidgetParameters(emptyMap());
        response.setValidatorMessages(new ValidatorMessages());
    }

    protected void processActionResult(ClientRequest request, ServerResponse response, PageResponse pageResponse) {
        var controllerClass = pageResponse.getControllerClass();
        var controllerWrapper = widgetControllerWrapperByClass(controllerClass);
        request.getUrlParameters().putAll(pageResponse.getUrlParameters()); // TODO May be better to create a context class to avoid mutating the request
        request.getPathVariables().putAll(pageResponse.getPathVariables());
        invokeGetWidgetModelMethods(controllerWrapper, request, response);
        response.setHttpStatus(200);
    }


    protected void processFailedValidation(Map<String, ValidationError> validationErrors, ServerResponse response) {
        //response.setValidatorMessages();
        response.setHttpStatus(422);
    }

    protected void processActionResult(ClientRequest request, ServerResponse response, WidgetResponse widgetResponse) {
        var controllerClass = widgetResponse.getControllerClass();
        var controllerWrapper = widgetControllerWrapperByClass(controllerClass);
        request.getWidgetParameters().putAll(widgetResponse.getWidgetParameters()); // TODO May be better to create a context class to avoid mutating the request
        invokeGetWidgetModelMethods(controllerWrapper, request, response);
        response.setHttpStatus(200);
    }

    protected void processActionResult(ClientRequest request, ServerResponse response, Class<?> controllerClass) {
        if (controllerClass.isAnnotationPresent(Page.class)) {
            var controllerWrapper = pageControllerWrapperByClass(controllerClass);
            invokeGetPageModelMethods(controllerWrapper, request, response);
            response.setHttpStatus(200);
        } else if (controllerClass.isAnnotationPresent(Widget.class)) {
            var controllerWrapper = widgetControllerWrapperByClass(controllerClass);
            invokeGetWidgetModelMethods(controllerWrapper, request, response);
            response.setHttpStatus(200);
        } else {
            throw new IllegalStateException("returned type is not a controller class: " + controllerClass);
        }
    }

    protected void processPageResult(ClientRequest request, ServerResponse response, PageResponse pageResponse) {
        var controllerClass = pageResponse.getControllerClass();
        var controllerWrapper = pageControllerWrapperByClass(controllerClass);
        request.getPathVariables().putAll(pageResponse.getPathVariables());
        request.getUrlParameters().putAll(pageResponse.getUrlParameters());
        invokeGetPageModelMethods(controllerWrapper, request, response);
        response.setHttpStatus(200);
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
