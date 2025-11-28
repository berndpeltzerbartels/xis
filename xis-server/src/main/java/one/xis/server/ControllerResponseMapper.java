package one.xis.server;

import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;

@XISComponent
@RequiredArgsConstructor
class ControllerResponseMapper {

    void mapResultToResponse(ServerResponse response, ControllerResult result) {
        if (result.getActionProcessing() != null && result.getActionProcessing() != ActionProcessing.NONE) {
            response.setActionProcessing(result.getActionProcessing());
        }
        response.setUpdateEventKeys(result.getUpdateEventKeys());
        response.setNextURL(result.getNextURL());
        response.setData(result.getModelData());
        response.setFormData(result.getFormData());
        response.setNextWidgetId(result.getNextWidgetId());
        response.setValidatorMessages(result.getValidatorMessages());
        response.setWidgetContainerId(result.getWidgetContainerId());
        response.setReloadWidgets(result.getWidgetsToReload());
        response.getSessionStorageData().putAll(result.getSessionStorage());
        response.getLocalStorageData().putAll(result.getLocalStorage());
        response.getClientStorageData().putAll(result.getClientStorage());
        response.getGlobalVariableData().putAll(result.getGlobalVariables());
        response.setRedirectUrl(result.getRedirectUrl());
        response.getTagVariables().putAll(result.getTagVariables());
        response.getIdVariables().putAll(result.getIdVariables());
        if (response.getStatus() < 1)
            response.setStatus(result.isValidationFailed() ? 422 : 200);
        // TODO navigation test. reload widgets ? set widget in another container ?
    }
}
