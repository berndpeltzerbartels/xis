package one.xis.server;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import one.xis.*;
import one.xis.context.XISComponent;
import one.xis.deserialize.PostProcessingResult;
import one.xis.security.LocalLoginResponse;
import one.xis.validation.ValidatorMessageResolver;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.emptyMap;

@XISComponent
@RequiredArgsConstructor
class ControllerMethodResultMapper {

    private final ValidatorMessageResolver validatorMessageResolver;
    private final PathResolver pathResolver;

    void mapReturnValueToResult(ControllerMethodResult controllerMethodResult, Method method, Object returnValue, Map<String, Object> requestScope) {
        if (returnValue instanceof PageResponse pageResponse) {
            mapPageResponse(pageResponse, controllerMethodResult);
        } else if (returnValue instanceof WidgetResponse widgetResponse) {
            mapWidgetResponse(widgetResponse, controllerMethodResult);
        } else if (returnValue instanceof Class<?> controllerClass) {
            updateController(controllerMethodResult, controllerClass, emptyMap());
        } else if (returnValue instanceof RedirectControllerResponse redirectControllerResponse) {
            if (redirectControllerResponse instanceof LocalLoginResponse loginResponse) {
                controllerMethodResult.setTokens(loginResponse.getTokens());
            }
            controllerMethodResult.setRedirectUrl(redirectControllerResponse.getRedirectUrl());
            controllerMethodResult.getUrlParameters().putAll(redirectControllerResponse.getUrlParameters());
        }
        if (method.isAnnotationPresent(ModelData.class)) {
            mapModelResult(method.getAnnotation(ModelData.class).value(), returnValue, controllerMethodResult);
        }
        if (method.isAnnotationPresent(FormData.class)) {
            mapFormData(method.getAnnotation(FormData.class).value(), returnValue, controllerMethodResult);
        }
        if (method.isAnnotationPresent(RequestScope.class)) {
            var key = method.getAnnotation(RequestScope.class).value();
            requestScope.put(key, returnValue);
        }
        if (method.isAnnotationPresent(ClientState.class)) {
            var key = method.getAnnotation(ClientState.class).value();
            controllerMethodResult.getClientState().put(key, returnValue);
        }
        if (method.isAnnotationPresent(LocalStorage.class)) {
            var key = method.getAnnotation(LocalStorage.class).value();
            controllerMethodResult.getLocalStorage().put(key, returnValue);
        }
    }

    void mapMethodParameterToResultAfterInvocation(ControllerMethodResult controllerMethodResult, ControllerMethodParameter[] parameters, Object[] args) {
        for (var i = 0; i < parameters.length; i++) {
            parameters[i].addParameterValueToResult(controllerMethodResult, args[i]);
        }
    }

    void mapRequestToResult(ClientRequest request, ControllerMethodResult controllerMethodResult) {
        // Do not map widgetId or pageURL here !
        controllerMethodResult.setWidgetContainerId(request.getWidgetContainerId());
        controllerMethodResult.getWidgetParameters().putAll(castStringMap(request.getBindingParameters()));
        controllerMethodResult.getPathVariables().putAll(castStringMap(request.getPathVariables()));
        controllerMethodResult.getUrlParameters().putAll(castStringMap(request.getUrlParameters()));
    }

    void mapValidationErrors(ControllerMethodResult controllerMethodResult, Collection<PostProcessingResult> errors) {
        controllerMethodResult.getValidatorMessages().getGlobalMessages().addAll(mapGlobalErrors(errors));
        controllerMethodResult.getValidatorMessages().getMessages().putAll(mapErrors(errors));
        controllerMethodResult.setValidationFailed(true);
    }

    private void mapModelResult(String key, Object value, ControllerMethodResult controllerMethodResult) {
        controllerMethodResult.getModelData().put(key, value);
    }

    private void mapFormData(String key, Object value, ControllerMethodResult controllerMethodResult) {
        controllerMethodResult.getFormData().put(key, value);
    }

    private void mapWidgetResponse(WidgetResponse widgetResponse, ControllerMethodResult result) {
        if (widgetResponse.getControllerClass() != null) {
            updateController(result, widgetResponse.getControllerClass(), emptyMap());
        }
        if (widgetResponse.getTargetContainer() != null) {
            result.setWidgetContainerId(widgetResponse.getTargetContainer());
        }
        if (widgetResponse.getWidgetsToReload() != null) {
            result.getWidgetsToReload().addAll(widgetResponse.getWidgetsToReload());
        }

        if (widgetResponse.getWidgetParameters() != null) {
            result.getWidgetParameters().putAll(widgetResponse.getWidgetParameters());
        }

    }

    private void mapPageResponse(PageResponse pageResponse, ControllerMethodResult controllerMethodResult) {
        if (pageResponse.getControllerClass() != null) {
            updateController(controllerMethodResult, pageResponse.getControllerClass(), pageResponse.getPathVariables());
        }
        if (pageResponse.getPathVariables() != null) {
            controllerMethodResult.getPathVariables().putAll(pageResponse.getPathVariables());
        }
        if (pageResponse.getUrlParameters() != null) {
            controllerMethodResult.getUrlParameters().putAll(pageResponse.getUrlParameters());
        }
    }

    private void updateController(@NonNull ControllerMethodResult result, @NonNull Class<?> controllerClass, Map<String, Object> pathVariables) {
        if (controllerClass.isAnnotationPresent(Widget.class)) {
            result.setNextWidgetId(WidgetUtil.getId(controllerClass));
        } else if (controllerClass.isAnnotationPresent(Page.class)) {
            var realPath = pathResolver.createPath(PageUtil.getUrl(controllerClass));
            var pathString = pathResolver.evaluateRealPath(realPath, pathVariables, emptyMap());
            result.setNextURL(pathString);
            result.setNextPageId(realPath.normalized());
        } else {
            throw new IllegalStateException("not a widget-controller or page-controller:" + controllerClass);
        }
    }

    private Map<String, Object> castStringMap(Map<String, String> map) {
        return map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map<String, String> mapErrors(Collection<PostProcessingResult> errors) {
        var errorMessageMap = new HashMap<String, String>();
        errors.forEach(error -> mapError(error, errorMessageMap));
        return errorMessageMap;
    }

    private List<String> mapGlobalErrors(Collection<PostProcessingResult> errors) {
        return errors.stream().map(this::globalErrorMessages).toList();
    }

    private void mapError(PostProcessingResult error, Map<String, String> errorMessageMap) {
        var key = error.getDeserializationContext().getPath();
        if (errorMessageMap.containsKey(key)) {
            return;
        }
        var parameterMap = new HashMap<String, Object>(errorMessageMap);
        parameterMap.put("value", error.getValue());
        var message = validatorMessageResolver.createMessage(error.getMessageKey(),
                parameterMap,
                error.getDeserializationContext().getTarget(),
                error.getDeserializationContext().getUserContext());
        errorMessageMap.put(key, message);
    }

    private String globalErrorMessages(PostProcessingResult error) {
        return validatorMessageResolver.createMessage(error.getGlobalMessageKey(),
                error.getMessageParameters(),
                error.getDeserializationContext().getTarget(),
                error.getDeserializationContext().getUserContext());
    }
}
