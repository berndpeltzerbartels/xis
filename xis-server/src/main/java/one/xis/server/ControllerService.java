package one.xis.server;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import one.xis.context.XISComponent;
import one.xis.context.XISInject;
import one.xis.utils.lang.StringUtils;

@Slf4j
@XISComponent
class ControllerService {

    @XISInject
    private ControllerResponseMapper responseMapper;

    @XISInject
    private PageControllerWrappers pageControllerWrappers;

    @XISInject
    private WidgetControllerWrappers widgetControllerWrappers;

    @XISInject
    private ControllerResultMapper controllerResultMapper;

    @XISInject
    private PathResolver pathResolver;

    void processModelDataRequest(@NonNull ClientRequest request, @NonNull ServerResponse response) {
        var controllerResult = new ControllerResult();
        controllerResult.setCurrentPageURL(request.getPageId());
        controllerResult.setCurrentWidgetId(request.getWidgetId());
        var wrapper = controllerWrapper(request);
        wrapper.invokeGetModelMethods(request, controllerResult);
        if (controllerResult.getNextPageURL() == null) {
            controllerResult.setNextPageURL(request.getPageId());
        }
        if (controllerResult.getNextWidgetId() == null) {
            controllerResult.setNextWidgetId(request.getWidgetId());
        }
        mapResultToResponse(response, controllerResult);
    }

    void processFormDataRequest(@NonNull ClientRequest request, @NonNull ServerResponse response) {
        var controllerResult = new ControllerResult();
        controllerResult.setCurrentPageURL(request.getPageId());
        controllerResult.setCurrentWidgetId(request.getWidgetId());
        var wrapper = controllerWrapper(request);
        wrapper.invokeFormDataMethods(request, controllerResult);
        if (controllerResult.getNextPageURL() == null) {
            controllerResult.setNextPageURL(request.getPageId());
        }
        if (controllerResult.getNextWidgetId() == null) {
            controllerResult.setNextWidgetId(request.getWidgetId());
        }
        mapResultToResponse(response, controllerResult);
    }

    void processActionRequest(@NonNull ClientRequest request, @NonNull ServerResponse response) {
        var controllerResult = new ControllerResult();
        controllerResult.setCurrentPageURL(request.getPageId());
        controllerResult.setCurrentWidgetId(request.getWidgetId());
        var invokerControllerWrapper = controllerWrapper(request);
        invokerControllerWrapper.invokeActionMethod(request, controllerResult);
        if (controllerResult.getNextPageURL() == null) {
            controllerResult.setNextPageURL(request.getPageId());
        }
        if (controllerResult.getNextWidgetId() == null) {
            controllerResult.setNextWidgetId(request.getWidgetId());
        }
        mapResultToResponse(response, controllerResult);
        var nextControllerWrapper = nextControllerWrapperAfterAction(controllerResult);
        if (nextControllerWrapper.equals(invokerControllerWrapper)) {
            mapResultToResponse(response, controllerResult);
        } else {
            processNextController(request, controllerResult, response, nextControllerWrapper);
        }
    }

    private void processNextController(ClientRequest request, ControllerResult controllerResult, ServerResponse response, ControllerWrapper nextControllerWrapper) {
        var nextRequest = new ClientRequest();
        // userdata is the same
        nextRequest.setLocale(request.getLocale());
        nextRequest.setZoneId(request.getZoneId());
        nextRequest.setClientId(request.getClientId());
        nextRequest.setUserId(request.getUserId());
        nextRequest.getLocalStorageData().putAll(request.getLocalStorageData());
        nextRequest.getClientStateData().putAll(request.getClientStateData());
        controllerResultMapper.mapControllerResultToRequest(controllerResult, nextRequest);
        var nextControllerResult = new ControllerResult();
        // one of these 2 values changed
        nextControllerResult.setNextPageURL(controllerResult.getNextPageURL());
        nextControllerResult.setNextWidgetId(controllerResult.getNextWidgetId());
        // get model data for next controller
        nextControllerWrapper.invokeGetModelMethods(nextRequest, nextControllerResult);
        // map result to response
        mapResultToResponse(response, nextControllerResult);
    }

    private ControllerWrapper controllerWrapper(ClientRequest request) {
        if (StringUtils.isNotEmpty(request.getWidgetId())) {
            return widgetControllerWrapperById(request.getWidgetId());
        }
        return pageControllerWrapperById(request.getPageId());
    }

    private ControllerWrapper nextControllerWrapperAfterAction(@NonNull ControllerResult controllerResult) {
        if (StringUtils.isNotEmpty(controllerResult.getNextPageURL())) {
            return pageControllerWrapperById(controllerResult.getNextPageURL());
        }
        if (StringUtils.isNotEmpty(controllerResult.getNextWidgetId())) {
            return widgetControllerWrapperById(controllerResult.getNextWidgetId());
        }
        throw new IllegalStateException("no controller found for request: " + controllerResult);
    }

    private void mapResultToResponse(ServerResponse response, ControllerResult result) {
        responseMapper.mapResultToResponse(response, result);
    }

    protected ControllerWrapper widgetControllerWrapperById(@NonNull String id) {
        return widgetControllerWrappers.findWidgetById(id)
                .orElseThrow(() -> new IllegalStateException("not a widget-controller:" + id));
    }

    protected ControllerWrapper pageControllerWrapperById(@NonNull String id) {
        return pageControllerWrappers.findByPath(pathResolver.createPath(id))
                .orElseThrow(() -> new IllegalStateException("page-controller not found for path:" + id));
    }

}
