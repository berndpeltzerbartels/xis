package one.xis.server;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import one.xis.Page;
import one.xis.context.Component;
import one.xis.context.Inject;
import one.xis.utils.lang.StringUtils;
import org.tinylog.Logger;

@Slf4j
@Component
class ControllerService {

    @Inject
    private ControllerResponseMapper responseMapper;

    @Inject
    private PageControllerWrappers pageControllerWrappers;

    @Inject
    private WidgetControllerWrappers widgetControllerWrappers;

    @Inject
    private ControllerResultMapper controllerResultMapper;

    @Inject
    private PathResolver pathResolver;

    void processModelDataRequest(@NonNull ClientRequest request, @NonNull ServerResponse response) {
        Logger.info("Process model data request: {}", request);
        var controllerResult = new ControllerResult();
        controllerResult.setCurrentPageURL(request.getPageId());
        controllerResult.setCurrentWidgetId(request.getWidgetId());
        var wrapper = controllerWrapper(request);
        wrapper.invokeGetModelMethods(request, controllerResult);
        if (controllerResult.getNextWidgetId() == null) {
            controllerResult.setNextWidgetId(request.getWidgetId());
        }
        //       if (request.getType() == RequestType.page) {
        response.getDefaultWidgets().addAll(widgetControllerWrappers.findDefaultWidgetsByPageUrl(request.getPageUrl()));
        //     }
        mapResultToResponse(request, response, controllerResult);
    }

    void processFormDataRequest(@NonNull ClientRequest request, @NonNull ServerResponse response) {
        Logger.info("Process form data request: {}", request);
        var controllerResult = new ControllerResult();
        controllerResult.setCurrentPageURL(request.getPageId());
        controllerResult.setCurrentWidgetId(request.getWidgetId());
        var wrapper = controllerWrapper(request);
        wrapper.invokeFormDataMethods(request, controllerResult);
        if (controllerResult.getNextWidgetId() == null) {
            controllerResult.setNextWidgetId(request.getWidgetId());
        }
        mapResultToResponse(request, response, controllerResult);
    }

    void processActionRequest(@NonNull ClientRequest request, @NonNull ServerResponse response) {
        Logger.info("Process action request: {}", request);
        if (request.getAction() == null) {
            throw new IllegalArgumentException("action is null");
        }
        var controllerResult = new ControllerResult();
        controllerResult.setCurrentPageURL(request.getPageId());
        controllerResult.setCurrentWidgetId(request.getWidgetId());
        var invokerControllerWrapper = controllerWrapper(request);
        invokerControllerWrapper.invokeActionMethod(request, controllerResult);
        if (!resultContainsNextController(controllerResult)) {
            usePreviousControllerAfterAction(controllerResult, invokerControllerWrapper, request);
        }
        mapResultToResponse(request, response, controllerResult);
        var nextControllerWrapper = nextControllerWrapperAfterAction(controllerResult);
        if (nextControllerWrapper.equals(invokerControllerWrapper)) {
            invokerControllerWrapper.invokeGetModelMethods(request, controllerResult);
            mapResultToResponse(request, response, controllerResult);
        } else {
            processNextController(request, controllerResult, response, nextControllerWrapper);
        }
    }

    private void processNextController(ClientRequest request, ControllerResult controllerResult, ServerResponse response, ControllerWrapper nextControllerWrapper) {
        Logger.info("Process next controller: {}, request: {}", nextControllerWrapper, request);
        var nextRequest = new ClientRequest();
        // userdata is the same
        nextRequest.setLocale(request.getLocale());
        nextRequest.setZoneId(request.getZoneId());
        nextRequest.setClientId(request.getClientId());
        nextRequest.getLocalStorageData().putAll(request.getLocalStorageData());
        nextRequest.getSessionStorageData().putAll(request.getSessionStorageData());
        nextRequest.setAccessToken(request.getAccessToken());
        controllerResultMapper.mapControllerResultToNextRequest(controllerResult, nextRequest);
        var nextControllerResult = new ControllerResult();
        // one of these 2 values changed
        if (nextControllerWrapper.isWidgetController()) {
            nextControllerResult.setNextWidgetId(nextControllerWrapper.getId());
        } else {
            var path = pathResolver.createPath(nextControllerWrapper.getController().getClass().getAnnotation(Page.class).value());
            nextControllerResult.setNextURL(this.pathResolver.evaluateRealPath(path, controllerResult.getPathVariables(), controllerResult.getUrlParameters()));
        }
        // get model data for next controller
        nextControllerWrapper.invokeGetModelMethods(nextRequest, nextControllerResult);
        // map result to response
        response.clear();
        mapResultToResponse(request, response, nextControllerResult);
    }

    private ControllerWrapper controllerWrapper(ClientRequest request) {
        if (request.getType() == RequestType.widget) {
            return widgetControllerWrapperById(request.getWidgetId());
        } else if (request.getType() == RequestType.page) {
            return pageControllerWrapperById(request.getPageId());
        }
        return pageControllerWrapperById(request.getPageId());
    }

    private boolean resultContainsNextController(ControllerResult controllerResult) {
        return StringUtils.isNotEmpty(controllerResult.getNextWidgetId()) || StringUtils.isNotEmpty(controllerResult.getNextURL());
    }


    private void usePreviousControllerAfterAction(ControllerResult controllerResult, ControllerWrapper controllerWrapper, ClientRequest request) {
        if (controllerWrapper.isWidgetController()) {
            controllerResult.setNextWidgetId(controllerWrapper.getId());
            controllerResult.setActionProcessing(ActionProcessing.WIDGET);
        } else {
            controllerResult.setNextURL(request.getPageUrl());
            controllerResult.setNextPageId(request.getPageId());
            controllerResult.setActionProcessing(ActionProcessing.PAGE);
        }
    }

    private ControllerWrapper nextControllerWrapperAfterAction(@NonNull ControllerResult controllerResult) {
        if (StringUtils.isNotEmpty(controllerResult.getNextWidgetId())) {
            return widgetControllerWrapperById(controllerResult.getNextWidgetId());
        }
        if (controllerResult.getNextPageControllerClass() != null) {
            return pageControllerWrapperByClass(controllerResult.getNextPageControllerClass());
        }
        if (controllerResult.getNextPageId() != null) {
            return pageControllerWrapperById(controllerResult.getNextPageId());
        }
        throw new IllegalStateException("no controller found for request: " + controllerResult);
    }

    private String getNextUrl(ClientRequest request, ControllerResult controllerResult) {
        if (StringUtils.isNotEmpty(controllerResult.getNextURL())) {
            return controllerResult.getNextURL();
        }
        if (controllerResult.getNextPageControllerClass() != null) {
            var path = pathResolver.createPath(PageUtil.getUrl(controllerResult.getNextPageControllerClass()));
            return pathResolver.evaluateRealPath(path, controllerResult.getPathVariables(), controllerResult.getUrlParameters());
        }
        return request.getPageUrl();
    }

    private void mapResultToResponse(ClientRequest request, ServerResponse response, ControllerResult controllerResult) {
        if (controllerResult.getNextURL() == null) {
            controllerResult.setNextURL(getNextUrl(request, controllerResult));
        }
        responseMapper.mapResultToResponse(response, controllerResult);
    }

    protected ControllerWrapper widgetControllerWrapperById(@NonNull String id) {
        return widgetControllerWrappers.findWidgetById(id)
                .orElseThrow(() -> new IllegalStateException("not a widget-controller:" + id));
    }

    protected ControllerWrapper pageControllerWrapperByClass(@NonNull Class<?> controllerClass) {
        return pageControllerWrappers.findByClass(controllerClass)
                .orElseThrow(() -> new IllegalStateException("page-controller not found:" + controllerClass.getSimpleName()));
    }


    protected ControllerWrapper pageControllerWrapperById(@NonNull String normalizedPath) {
        return pageControllerWrappers.findByPath(normalizedPath)
                .orElseThrow(() -> new IllegalStateException("page-controller not found:" + normalizedPath));
    }

    protected ControllerWrapper pageControllerWrapperByPath(@NonNull String realPath) {
        return pageControllerWrappers.findByRealPath(realPath).map(PageControllerMatch::getPageControllerWrapper)
                .orElseThrow(() -> new IllegalStateException("page-controller not found for path:" + realPath));
    }

}
