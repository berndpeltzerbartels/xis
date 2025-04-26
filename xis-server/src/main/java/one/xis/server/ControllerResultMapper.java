package one.xis.server;

import one.xis.context.XISComponent;

import java.util.Map;
import java.util.stream.Collectors;

@XISComponent
class ControllerResultMapper {

    void mapMethodResultToControllerResult(ControllerMethodResult controllerMethodResult, ControllerResult controllerResult) {
        if (controllerMethodResult.getNextPageURL() != null) {
            controllerResult.setNextPageURL(controllerMethodResult.getNextPageURL());
        }
        if (controllerMethodResult.getNextWidgetId() != null) {
            controllerResult.setNextWidgetId(controllerMethodResult.getNextWidgetId());
        }
        if (controllerMethodResult.getWidgetContainerId() != null) {
            controllerResult.setWidgetContainerId(controllerMethodResult.getWidgetContainerId());
        }
        controllerResult.getModelData().putAll(controllerMethodResult.getModelData());
        controllerResult.getFormData().putAll(controllerMethodResult.getFormData());
        controllerResult.getBindingParameters().putAll(controllerMethodResult.getWidgetParameters());
        controllerResult.getPathVariables().putAll(controllerMethodResult.getPathVariables());
        controllerResult.getUrlParameters().putAll(controllerMethodResult.getUrlParameters());
        controllerResult.getValidatorMessages().getGlobalMessages().addAll(controllerMethodResult.getValidatorMessages().getGlobalMessages());
        controllerResult.getValidatorMessages().getMessages().putAll(controllerMethodResult.getValidatorMessages().getMessages());
        controllerResult.getWidgetsToReload().addAll(controllerMethodResult.getWidgetsToReload());
        controllerResult.getRequestScope().putAll(controllerMethodResult.getRequestScope());
        controllerResult.getClientScope().putAll(controllerMethodResult.getClientScope());
        controllerResult.getLocalStorage().putAll(controllerMethodResult.getLocalStorage());
        if (controllerMethodResult.isValidationFailed()) {
            controllerResult.setValidationFailed(true);
        }
    }

    void mapControllerResultToRequest(ControllerResult controllerResult, ClientRequest nextRequest) {
        nextRequest.setPageId(controllerResult.getNextPageURL());
        nextRequest.setWidgetId(controllerResult.getNextWidgetId());
        nextRequest.setFormData(controllerResult.getFormData().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString())));
        nextRequest.setUrlParameters(controllerResult.getUrlParameters().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString())));
        nextRequest.setPathVariables(controllerResult.getPathVariables().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString())));
        nextRequest.setWidgetContainerId(controllerResult.getWidgetContainerId());
        nextRequest.setBindingParameters(controllerResult.getBindingParameters().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString())));
    }
}
