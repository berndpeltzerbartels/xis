package one.xis.server;

import com.google.gson.Gson;
import one.xis.context.Component;
import one.xis.gson.JsonMap;

import java.util.Map;
import java.util.stream.Collectors;

@Component
class ControllerResultMapper {

    private final Gson gson = new Gson();

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
        if (controllerMethodResult.getNextModalId() != null) {
            controllerResult.setNextModalId(controllerMethodResult.getNextModalId());
        }
        if (controllerMethodResult.isCloseModal()) {
            controllerResult.setCloseModal(true);
        }
        if (controllerMethodResult.isReloadModalParent()) {
            controllerResult.setReloadModalParent(true);
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
        controllerResult.getReturnedFormDataKeys().addAll(controllerMethodResult.getReturnedFormDataKeys());
        controllerResult.getFrontletParameters().putAll(controllerMethodResult.getFrontletParameters());
        controllerResult.getModalParameters().putAll(controllerMethodResult.getModalParameters());
        controllerResult.getPathVariables().putAll(controllerMethodResult.getPathVariables());
        controllerResult.getUrlParameters().putAll(controllerMethodResult.getUrlParameters());
        controllerResult.getValidatorMessages().getGlobalMessages().addAll(controllerMethodResult.getValidatorMessages().getGlobalMessages());
        controllerResult.getValidatorMessages().getMessages().putAll(controllerMethodResult.getValidatorMessages().getMessages());
        controllerResult.getFrontletsToReload().addAll(controllerMethodResult.getFrontletsToReload());
        controllerResult.getSessionStorage().putAll(controllerMethodResult.getSessionStorage());
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
        nextRequest.setFrontletParameters(toJsonValueMap(controllerResult.getFrontletParameters()));
        nextRequest.setModalParameters(toJsonValueMap(controllerResult.getModalParameters()));
        nextRequest.setFrontletId(controllerResult.getNextFrontletId() != null ? controllerResult.getNextFrontletId() : controllerResult.getNextModalId());
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

    private JsonMap toJsonValueMap(Map<?, ?> data) {
        JsonMap jsonMap = new JsonMap();
        for (Map.Entry<?, ?> entry : data.entrySet()) {
            jsonMap.put(String.valueOf(entry.getKey()), gson.toJson(entry.getValue()));
        }
        return jsonMap;
    }
}
