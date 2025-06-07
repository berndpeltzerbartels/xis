package one.xis.server;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import one.xis.context.XISComponent;
import one.xis.context.XISInject;
import one.xis.utils.lang.StringUtils;
import org.tinylog.Logger;

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
        Logger.info("Process model data request: {}", request);
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
        Logger.info("Process form data request: {}", request);
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
        Logger.info("Process action request: {}", request);
        var controllerResult = new ControllerResult();
        controllerResult.setCurrentPageURL(request.getPageId());
        controllerResult.setCurrentWidgetId(request.getWidgetId());
        var invokerControllerWrapper = controllerWrapper(request);
        invokerControllerWrapper.invokeActionMethod(request, controllerResult);
        if (!resultContainsNextController(controllerResult)) {
            usePreviousController(controllerResult, invokerControllerWrapper);
        }
        mapResultToResponse(response, controllerResult);
        var nextControllerWrapper = nextControllerWrapperAfterAction(controllerResult);
        if (nextControllerWrapper.equals(invokerControllerWrapper)) {
            invokerControllerWrapper.invokeGetModelMethods(request, controllerResult);
            mapResultToResponse(response, controllerResult);
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
        nextRequest.setUserId(request.getUserId());
        nextRequest.getLocalStorageData().putAll(request.getLocalStorageData());
        nextRequest.getClientStateData().putAll(request.getClientStateData());
        controllerResultMapper.mapControllerResultToNextRequest(controllerResult, nextRequest);
        var nextControllerResult = new ControllerResult();
        // one of these 2 values changed
        if (nextControllerWrapper.isWidgetController()) {
            nextControllerResult.setNextWidgetId(nextControllerWrapper.getId());
        } else {
            nextControllerResult.setNextPageURL(nextControllerWrapper.getId());
        }
        // get model data for next controller
        nextControllerWrapper.invokeGetModelMethods(nextRequest, nextControllerResult);
        // map result to response
        response.clear();
        mapResultToResponse(response, nextControllerResult);
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
        return StringUtils.isNotEmpty(controllerResult.getNextWidgetId()) || StringUtils.isNotEmpty(controllerResult.getNextPageURL());
    }


    private void usePreviousController(ControllerResult controllerResult, ControllerWrapper controllerWrapper) {
        if (controllerWrapper.isWidgetController()) {
            controllerResult.setNextWidgetId(controllerWrapper.getId());
        } else {
            controllerResult.setNextPageURL(controllerWrapper.getId());
        }
    }

    private ControllerWrapper nextControllerWrapperAfterAction(@NonNull ControllerResult controllerResult) {
        if (StringUtils.isNotEmpty(controllerResult.getNextWidgetId())) {
            return widgetControllerWrapperById(controllerResult.getNextWidgetId());
        }
        if (StringUtils.isNotEmpty(controllerResult.getNextPageURL())) {
            return pageControllerWrapperById(controllerResult.getNextPageURL());
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
