package one.xis.server;

import one.xis.context.XISComponent;

import java.util.Map;
import java.util.stream.Collectors;

@XISComponent
class ControllerResultMapper {

    void mapMethodResultToControllerResult(ControllerMethodResult controllerMethodResult, ControllerResult controllerResult) {
        if (controllerMethodResult.getNextURL() != null) {
            controllerResult.setNextURL(controllerMethodResult.getNextURL());
        }
        if (controllerMethodResult.getNextPageId() != null) {
            controllerResult.setNextPageId(controllerMethodResult.getNextPageId());
        }
        if (controllerMethodResult.getNextWidgetId() != null) {
            controllerResult.setNextWidgetId(controllerMethodResult.getNextWidgetId());
        }
        if (controllerMethodResult.getWidgetContainerId() != null) {
            controllerResult.setWidgetContainerId(controllerMethodResult.getWidgetContainerId());
        }
        controllerResult.setTokens(controllerMethodResult.getTokens());
        controllerResult.getModelData().putAll(controllerMethodResult.getModelData());
        controllerResult.getFormData().putAll(controllerMethodResult.getFormData());
        controllerResult.getBindingParameters().putAll(controllerMethodResult.getWidgetParameters());
        controllerResult.getPathVariables().putAll(controllerMethodResult.getPathVariables());
        controllerResult.getUrlParameters().putAll(controllerMethodResult.getUrlParameters());
        controllerResult.getValidatorMessages().getGlobalMessages().addAll(controllerMethodResult.getValidatorMessages().getGlobalMessages());
        controllerResult.getValidatorMessages().getMessages().putAll(controllerMethodResult.getValidatorMessages().getMessages());
        controllerResult.getWidgetsToReload().addAll(controllerMethodResult.getWidgetsToReload());
        controllerResult.getClientState().putAll(controllerMethodResult.getClientState());
        controllerResult.getRequestScope().putAll(controllerMethodResult.getRequestScope());
        controllerResult.getLocalStorage().putAll(controllerMethodResult.getLocalStorage());
        if (controllerMethodResult.isValidationFailed()) {
            controllerResult.setValidationFailed(true);
        }
    }

    void mapControllerResultToNextRequest(ControllerResult controllerResult, ClientRequest nextRequest) {
        nextRequest.getUrlParameters().putAll(controllerResult.getUrlParameters().entrySet().stream().map(e -> Map.entry(e.getKey(), e.getValue().toString())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        nextRequest.setPathVariables(controllerResult.getPathVariables().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString())));
        nextRequest.setWidgetContainerId(controllerResult.getWidgetContainerId());
        nextRequest.setBindingParameters(controllerResult.getBindingParameters().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString())));
        nextRequest.setWidgetId(controllerResult.getNextWidgetId());
    }
}
