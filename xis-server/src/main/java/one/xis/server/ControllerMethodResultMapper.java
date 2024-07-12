package one.xis.server;

import lombok.NonNull;
import one.xis.*;
import one.xis.context.XISComponent;
import one.xis.validation.ValidationErrors;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.stream.Collectors;

@XISComponent
class ControllerMethodResultMapper {

    // TODO validate widgetresponse must have target container when return from PageController
    ControllerMethodResult mapControllerResult(Method method, Object returnValue) {
        var controllerMethodResult = new ControllerMethodResult();
        if (method.isAnnotationPresent(Action.class)) {
            mapActionResult(returnValue, controllerMethodResult);
        }
        if (method.isAnnotationPresent(ModelData.class)) {
            mapModelResult(method.getAnnotation(ModelData.class).value(), returnValue, controllerMethodResult);
        }
        if (method.isAnnotationPresent(FormData.class)) {
            mapModelResult(method.getAnnotation(FormData.class).value(), returnValue, controllerMethodResult);
        }
        return controllerMethodResult;
    }

    ControllerMethodResult keepStateOnAction(ClientRequest request, Method method) {
        if (!method.isAnnotationPresent(Action.class)) {
            throw new IllegalArgumentException("Method must be an action method");
        }
        var controllerMethodResult = new ControllerMethodResult();
        controllerMethodResult.setNextPageURL(request.getPageId());
        controllerMethodResult.setNextWidgetId(request.getWidgetId());
        controllerMethodResult.setWidgetContainerId(request.getWidgetContainerId());
        controllerMethodResult.setWidgetParameters(castStringMap(request.getWidgetParameters()));
        controllerMethodResult.setPathVariables(castStringMap(request.getPathVariables()));
        controllerMethodResult.setUrlParameters(castStringMap(request.getUrlParameters()));
        return controllerMethodResult;
    }

    ControllerMethodResult mapValidationErrorState(ClientRequest request, ValidationErrors errors) {
        var controllerMethodResult = new ControllerMethodResult();
        controllerMethodResult.setNextPageURL(request.getPageId());
        controllerMethodResult.setWidgetContainerId(request.getWidgetContainerId());
        controllerMethodResult.setWidgetParameters(castStringMap(request.getWidgetParameters()));
        controllerMethodResult.setPathVariables(castStringMap(request.getPathVariables()));
        controllerMethodResult.setUrlParameters(castStringMap(request.getUrlParameters()));
        controllerMethodResult.getValidatorMessages().getGlobalMessages().addAll(errors.getGlobalErrors());
        controllerMethodResult.getValidatorMessages().getMessages().putAll(errors.getErrors());
        controllerMethodResult.setValidationFailed(true);
        return controllerMethodResult;
    }

    private void mapModelResult(String key, Object value, ControllerMethodResult controllerMethodResult) {
        controllerMethodResult.getModelData().put(key, value);
    }

    private void mapActionResult(Object returnValue, ControllerMethodResult controllerMethodResult) {
        if (returnValue instanceof PageResponse pageResponse) {
            mapPageResponse(pageResponse, controllerMethodResult);
        } else if (returnValue instanceof WidgetResponse widgetResponse) {
            mapWidgetResponse(widgetResponse, controllerMethodResult);
        } else if (returnValue instanceof Class<?> controllerClass) {
            updateControllerClass(controllerMethodResult, controllerClass);
        } else {
            throw new IllegalStateException(returnValue.getClass() + " must be a widget class or an instance of WidgetResponse");
        }
    }

    private void mapWidgetResponse(WidgetResponse widgetResponse, ControllerMethodResult result) {
        if (widgetResponse.getControllerClass() != null) {
            updateControllerClass(result, widgetResponse.getControllerClass());
        }
        result.setWidgetContainerId(widgetResponse.getTargetContainer());
        result.setWidgetsToReload(widgetResponse.getWidgetsToReload());
        result.setWidgetParameters(widgetResponse.getWidgetParameters());
    }

    private void mapPageResponse(PageResponse pageResponse, ControllerMethodResult controllerMethodResult) {
        if (pageResponse.getControllerClass() != null) {
            updateControllerClass(controllerMethodResult, pageResponse.getControllerClass());
        }
        controllerMethodResult.setPathVariables(pageResponse.getPathVariables());
        controllerMethodResult.setUrlParameters(pageResponse.getUrlParameters());
    }


    private void updateControllerClass(@NonNull ControllerMethodResult result, @NonNull Class<?> controllerClass) {
        if (controllerClass.isAnnotationPresent(Widget.class)) {
            result.setNextWidgetId(WidgetUtil.getId(controllerClass));
        } else if (controllerClass.isAnnotationPresent(Page.class)) {
            result.setNextPageURL(PageUtil.getUrl(controllerClass));
        } else {
            throw new IllegalStateException("not a widget-controller or page-controller:" + controllerClass);
        }
    }

    private Map<String, Object> castStringMap(Map<String, String> map) {
        return map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
