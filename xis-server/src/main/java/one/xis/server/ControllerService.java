package one.xis.server;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import one.xis.ModelDataLoad;
import one.xis.Page;
import one.xis.context.Component;
import one.xis.context.Inject;
import one.xis.utils.lang.StringUtils;

import java.util.Set;

@Slf4j
@Component
class ControllerService {

    @Inject
    private ControllerResponseMapper responseMapper;

    @Inject
    private PageControllerWrappers pageControllerWrappers;

    @Inject
    private RouterControllerWrappers routerControllerWrappers;

    @Inject
    private FrontletControllerWrappers frontletControllerWrappers;

    @Inject
    private ControllerResultMapper controllerResultMapper;

    @Inject
    private PathResolver pathResolver;

    @Inject
    private ComponentHostResolver hostResolver;

    void processModelDataRequest(@NonNull ClientRequest request, @NonNull ServerResponse response) {
        if (request.getType() == RequestType.page && processRouterRequest(request, response)) {
            return;
        }
        var controllerResult = new ControllerResult();
        controllerResult.setCurrentPageURL(request.getPageId());
        controllerResult.setCurrentFrontletId(request.getFrontletId());
        var wrapper = controllerWrapper(request);
        wrapper.invokeGetModelMethods(request, controllerResult);
        if (controllerResult.getNextFrontletId() == null) {
            controllerResult.setNextFrontletId(request.getFrontletId());
        }
        //       if (request.getType() == RequestType.page) {
        response.getDefaultFrontlets().addAll(frontletControllerWrappers.findDefaultFrontletsByPageUrl(request.getPageUrl()));
        //     }
        mapResultToResponse(request, response, controllerResult);
    }

    private boolean processRouterRequest(ClientRequest request, ServerResponse response) {
        var match = routerControllerWrappers.findByRealPath(request.getPageUrl());
        if (match.isEmpty()) {
            return false;
        }
        request.getPathVariables().clear();
        request.getPathVariables().putAll(match.get().pathVariables());
        request.getUrlParameters().clear();
        request.getUrlParameters().putAll(match.get().queryParameters());
        var controllerResult = new ControllerResult();
        controllerResult.setCurrentPageURL(request.getPageId());
        match.get().wrapper().invokeRouteMethod(request, controllerResult, match.get().method());
        if (!resultContainsNextController(controllerResult)) {
            throw new IllegalStateException("Router method did not return a navigation value: " + match.get().method());
        }
        mapResultToResponse(request, response, controllerResult);
        var nextControllerWrapper = nextControllerWrapperAfterAction(controllerResult);
        if (nextControllerWrapper == null) {
            return true;
        }
        processNextController(request, controllerResult, response, nextControllerWrapper);
        response.setNextURL(controllerResult.getNextURL());
        return true;
    }

    void processFormDataRequest(@NonNull ClientRequest request, @NonNull ServerResponse response) {
        var controllerResult = new ControllerResult();
        controllerResult.setCurrentPageURL(request.getPageId());
        controllerResult.setCurrentFrontletId(request.getFrontletId());
        var wrapper = controllerWrapper(request);
        wrapper.invokeFormDataMethods(request, controllerResult);
        if (controllerResult.getNextFrontletId() == null) {
            controllerResult.setNextFrontletId(request.getFrontletId());
        }
        mapResultToResponse(request, response, controllerResult);
    }

    void processActionRequest(@NonNull ClientRequest request, @NonNull ServerResponse response) {
        if (request.getAction() == null) {
            throw new IllegalArgumentException("action is null");
        }
        var controllerResult = new ControllerResult();
        controllerResult.setCurrentPageURL(request.getPageId());
        controllerResult.setCurrentFrontletId(request.getFrontletId());
        var invokerControllerWrapper = controllerWrapper(request);
        invokerControllerWrapper.invokeActionMethod(request, controllerResult);
        Set<String> actionModelDataKeys = Set.copyOf(controllerResult.getModelData().keySet());
        if (!resultContainsNextController(controllerResult)) {
            usePreviousControllerAfterAction(controllerResult, invokerControllerWrapper, request);
        }
        mapResultToResponse(request, response, controllerResult);
        var nextControllerWrapper = nextControllerWrapperAfterAction(controllerResult);
        if (nextControllerWrapper == null) {
            return;
        }
        if (nextControllerWrapper.equals(invokerControllerWrapper)) {
            invokerControllerWrapper.invokeGetModelMethods(nextRequest(request, controllerResult), controllerResult, actionModelDataKeys, ModelDataLoad.AFTER_ACTION);
            mapResultToResponse(request, response, controllerResult);
        } else {
            processNextController(request, controllerResult, response, nextControllerWrapper);
        }
    }

    private void processNextController(ClientRequest request, ControllerResult controllerResult, ServerResponse response, ControllerWrapper nextControllerWrapper) {
        var nextRequest = nextRequest(request, controllerResult);
        var nextControllerResult = new ControllerResult();
        // one of these 2 values changed
        if (nextControllerWrapper.isFrontletController()) {
            nextControllerResult.setNextFrontletId(nextControllerWrapper.getId());
        } else if (nextControllerWrapper.isModalController()) {
            nextControllerResult.setNextModalId(nextControllerWrapper.getId());
            nextControllerResult.setActionProcessing(ActionProcessing.MODAL);
        } else {
            var path = pathResolver.createPath(nextControllerWrapper.getController().getClass().getAnnotation(Page.class).value());
            nextControllerResult.setNextURL(this.pathResolver.evaluateRealPath(path, controllerResult.getPathVariables(), controllerResult.getUrlParameters()));
        }
        nextControllerResult.getFrontletParameters().putAll(controllerResult.getFrontletParameters());
        // get model data for next controller
        nextControllerWrapper.invokeGetModelMethods(nextRequest, nextControllerResult);
        // map result to response
        response.clear();
        mapResultToResponse(request, response, nextControllerResult);
    }

    private ClientRequest nextRequest(ClientRequest request, ControllerResult controllerResult) {
        var nextRequest = new ClientRequest();
        // userdata is the same
        nextRequest.setLocale(request.getLocale());
        nextRequest.setZoneId(request.getZoneId());
        nextRequest.setClientId(request.getClientId());
        nextRequest.getLocalStorageData().putAll(request.getLocalStorageData());
        nextRequest.getSessionStorageData().putAll(request.getSessionStorageData());
        nextRequest.setAccessToken(request.getAccessToken());
        controllerResultMapper.mapControllerResultToNextRequest(controllerResult, nextRequest);
        return nextRequest;
    }

    private ControllerWrapper controllerWrapper(ClientRequest request) {
        if (request.getType() == RequestType.frontlet) {
            return frontletControllerWrapperById(request.getFrontletId());
        } else if (request.getType() == RequestType.page) {
            return pageControllerWrapperById(request.getPageId());
        }
        return pageControllerWrapperById(request.getPageId());
    }

    private boolean resultContainsNextController(ControllerResult controllerResult) {
        return StringUtils.isNotEmpty(controllerResult.getNextFrontletId())
                || StringUtils.isNotEmpty(controllerResult.getNextModalId())
                || controllerResult.isCloseModal()
                || StringUtils.isNotEmpty(controllerResult.getNextURL());
    }


    private void usePreviousControllerAfterAction(ControllerResult controllerResult, ControllerWrapper controllerWrapper, ClientRequest request) {
        if (controllerWrapper.isFrontletController()) {
            controllerResult.setNextFrontletId(controllerWrapper.getId());
            controllerResult.setActionProcessing(ActionProcessing.FRONTLET);
        } else if (controllerWrapper.isModalController()) {
            controllerResult.setNextModalId(controllerWrapper.getId());
            controllerResult.setActionProcessing(ActionProcessing.MODAL);
        } else {
            controllerResult.setNextURL(request.getPageUrl());
            controllerResult.setNextPageId(request.getPageId());
            controllerResult.setActionProcessing(ActionProcessing.PAGE);
        }
    }

    private ControllerWrapper nextControllerWrapperAfterAction(@NonNull ControllerResult controllerResult) {
        if (StringUtils.isNotEmpty(controllerResult.getNextFrontletId())) {
            var frontlet = frontletControllerWrappers.findFrontletById(controllerResult.getNextFrontletId());
            if (frontlet.isPresent()) {
                return frontlet.get();
            }
            if (StringUtils.isNotEmpty(hostResolver.getFrontletHost(controllerResult.getNextFrontletId()))) {
                return null;
            }
            throw new IllegalStateException("not a frontlet-controller:" + controllerResult.getNextFrontletId());
        }
        if (StringUtils.isNotEmpty(controllerResult.getNextModalId())) {
            return frontletControllerWrappers.findFrontletById(controllerResult.getNextModalId())
                    .filter(ControllerWrapper::isModalController)
                    .orElseThrow(() -> new IllegalStateException("not a modal-controller:" + controllerResult.getNextModalId()));
        }
        if (controllerResult.isCloseModal()) {
            return null;
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

    protected ControllerWrapper frontletControllerWrapperById(@NonNull String id) {
        return frontletControllerWrappers.findFrontletById(id)
                .orElseThrow(() -> new IllegalStateException("not a frontlet-controller:" + id));
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
