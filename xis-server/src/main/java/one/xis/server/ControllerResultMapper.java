package one.xis.server;

import one.xis.context.Component;
import one.xis.gson.JsonMap;

import java.util.Map;
import java.util.stream.Collectors;

@Component
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
        if (controllerMethodResult.getRedirectUrl() != null) {
            controllerResult.setRedirectUrl(controllerMethodResult.getRedirectUrl());
        }
        if (controllerMethodResult.getActionProcessing() != null && controllerMethodResult.getActionProcessing() != ActionProcessing.NONE) {
            controllerResult.setActionProcessing(controllerMethodResult.getActionProcessing());
        }
        if (controllerMethodResult.getAnnotatedTitle() != null) {
            controllerResult.setAnnotatedTitle(controllerMethodResult.getAnnotatedTitle());
        }
        if (controllerMethodResult.getAnnotatedAddress() != null) {
            controllerResult.setAnnotatedAddress(controllerMethodResult.getAnnotatedAddress());
        }
        controllerResult.getUpdateEventKeys().addAll(controllerMethodResult.getUpdateEventKeys());
        controllerResult.getModelData().putAll(controllerMethodResult.getModelData());
        controllerResult.getFormData().putAll(controllerMethodResult.getFormData());
        controllerResult.getWidgetParameters().putAll(controllerMethodResult.getWidgetParameters());
        controllerResult.getPathVariables().putAll(controllerMethodResult.getPathVariables());
        controllerResult.getUrlParameters().putAll(controllerMethodResult.getUrlParameters());
        controllerResult.getValidatorMessages().getGlobalMessages().addAll(controllerMethodResult.getValidatorMessages().getGlobalMessages());
        controllerResult.getValidatorMessages().getMessages().putAll(controllerMethodResult.getValidatorMessages().getMessages());
        controllerResult.getWidgetsToReload().addAll(controllerMethodResult.getWidgetsToReload());
        controllerResult.getSessionStorage().putAll(controllerMethodResult.getSessionStorage());
        controllerResult.getRequestScope().putAll(controllerMethodResult.getRequestScope());
        controllerResult.getLocalStorage().putAll(controllerMethodResult.getLocalStorage());
        controllerResult.getClientStorage().putAll(controllerMethodResult.getClientStorage());
        if (controllerMethodResult.isValidationFailed()) {
            controllerResult.setValidationFailed(true);
        }
        controllerResult.getIdVariables().putAll(controllerMethodResult.getIdVariables());
    }

    void mapControllerResultToNextRequest(ControllerResult controllerResult, ClientRequest nextRequest) {
        nextRequest.getUrlParameters().putAll(controllerResult.getUrlParameters().entrySet().stream().map(e -> Map.entry(e.getKey(), e.getValue().toString())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        nextRequest.setPathVariables(toJsonMap(controllerResult.getPathVariables()));
        nextRequest.setWidgetContainerId(controllerResult.getWidgetContainerId());
        nextRequest.setWidgetParameters(toJsonMap(controllerResult.getWidgetParameters()));
        nextRequest.setWidgetId(controllerResult.getNextWidgetId());
    }


    private JsonMap toJsonMap(Map<?, ?> data) {
        JsonMap jsonMap = new JsonMap();
        for (Map.Entry<?, ?> entry : data.entrySet()) {
            String key = String.valueOf(entry.getKey());
            String value = String.valueOf(entry.getValue());
            jsonMap.put(key, value);
        }
        return jsonMap;
    }
}
