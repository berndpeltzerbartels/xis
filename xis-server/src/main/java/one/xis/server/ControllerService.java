package one.xis.server;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import one.xis.context.XISComponent;
import one.xis.context.XISInject;
import one.xis.utils.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

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
    private PathResolver pathResolver;

    void processModelDataRequest(@NonNull ClientRequest request, @NonNull ServerResponse response) {
        var controllerResult = new ControllerResult();
        var wrapper = controllerWrapper(request);
        wrapper.invokeGetModelMethods(request, controllerResult);
        mapResultToResponse(response, controllerResult);
        if (response.getNextPageURL() == null && wrapper instanceof PageControllerWrapper pageControllerWrapper) {
            response.setNextPageURL(pageControllerWrapper.getId());
        }
        if (response.getNextWidgetId() == null && wrapper instanceof WidgetControllerWrapper widgetControllerWrapper) {
            response.setNextWidgetId(widgetControllerWrapper.getId());
        }
    }

    void processActionRequest(@NonNull ClientRequest request, @NonNull ServerResponse response) {
        var controllerResult = new ControllerResult();
        var invokerControllerWrapper = controllerWrapper(request);
        invokerControllerWrapper.invokeActionMethod(request, controllerResult);
        mapResultToResponse(response, controllerResult);
        var nextControllerWrapper = nextControllerWrapper(controllerResult);
        // This should only occur when action method is called
        mapResultToRequestOnAction(request, controllerResult);
        if (!controllerResult.isValidationFailed()) {
            nextControllerWrapper.invokeGetModelMethods(request, controllerResult);
        }
        mapResultToResponse(response, controllerResult);
    }

    private ControllerWrapper controllerWrapper(ClientRequest request) {
        if (request.getWidgetId() != null && !request.getWidgetId().isEmpty()) {
            return widgetControllerWrapperById(request.getWidgetId());
        }
        return pageControllerWrapperById(request.getPageId());
    }

    private ControllerWrapper nextControllerWrapper(ControllerResult controllerResult) {
        if (StringUtils.isNotEmpty(controllerResult.getNextWidgetId())) {
            return widgetControllerWrapperById(controllerResult.getNextWidgetId());
        }
        return pageControllerWrapperById(controllerResult.getNextPageURL());
    }

    private void mapResultToResponse(ServerResponse response, ControllerResult result) {
        responseMapper.mapResultToResponse(response, result);
    }

    private void mapResultToRequestOnAction(ClientRequest request, ControllerResult result) {
        if (request.getUrlParameters() == null) {
            request.setUrlParameters(new HashMap<>());
        }
        if (request.getPathVariables() == null) {
            request.setPathVariables(new HashMap<>());
        }
        if (request.getWidgetParameters() == null) {
            request.setWidgetParameters(new HashMap<>());
        }
        if (result.getUrlParameters() != null) {
            request.getUrlParameters().putAll(toStringMap(result.getUrlParameters()));
        }
        if (result.getPathVariables() != null) {
            request.getPathVariables().putAll(toStringMap(result.getPathVariables()));
        }
        if (result.getWidgetParameters() != null) {
            request.getWidgetParameters().putAll(toStringMap(result.getWidgetParameters()));
        }
    }

    private static Map<String, String> toStringMap(Map<String, Object> map) {
        return map.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString()));
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
