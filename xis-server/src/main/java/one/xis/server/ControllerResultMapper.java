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
        if (controllerMethodResult.getNextFrontletId() != null) {
            controllerResult.setNextFrontletId(controllerMethodResult.getNextFrontletId());
        }
        if (controllerMethodResult.getFrontletContainerId() != null) {
            controllerResult.setFrontletContainerId(controllerMethodResult.getFrontletContainerId());
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
        controllerResult.getFrontletParameters().putAll(controllerMethodResult.getFrontletParameters());
        controllerResult.getPathVariables().putAll(controllerMethodResult.getPathVariables());
        controllerResult.getUrlParameters().putAll(controllerMethodResult.getUrlParameters());
        controllerResult.getValidatorMessages().getGlobalMessages().addAll(controllerMethodResult.getValidatorMessages().getGlobalMessages());
        controllerResult.getValidatorMessages().getMessages().putAll(controllerMethodResult.getValidatorMessages().getMessages());
        controllerResult.getFrontletsToReload().addAll(controllerMethodResult.getFrontletsToReload());
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
        nextRequest.setFrontletContainerId(controllerResult.getFrontletContainerId());
        nextRequest.setFrontletParameters(toJsonMap(controllerResult.getFrontletParameters()));
        nextRequest.setFrontletId(controllerResult.getNextFrontletId());
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
