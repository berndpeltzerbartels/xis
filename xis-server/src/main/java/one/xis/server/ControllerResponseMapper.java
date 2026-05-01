package one.xis.server;

import lombok.RequiredArgsConstructor;
import one.xis.context.Component;

@Component
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
        response.setNextFrontletId(result.getNextFrontletId());
        response.setValidatorMessages(result.getValidatorMessages());
        response.setFrontletContainerId(result.getFrontletContainerId());
        response.setReloadFrontlets(result.getFrontletsToReload());
        response.getSessionStorageData().putAll(result.getSessionStorage());
        response.getLocalStorageData().putAll(result.getLocalStorage());
        response.getClientStorageData().putAll(result.getClientStorage());
        response.setRedirectUrl(result.getRedirectUrl());
        response.getIdVariables().putAll(result.getIdVariables());
        response.getFrontletParameters().putAll(result.getFrontletParameters());
        if (result.getAnnotatedTitle() != null) {
            response.setAnnotatedTitle(result.getAnnotatedTitle());
        }
        if (result.getAnnotatedAddress() != null) {
            response.setAnnotatedAddress(result.getAnnotatedAddress());
        }
        if (response.getStatus() < 1)
            response.setStatus(result.isValidationFailed() ? 422 : 200);
        // TODO navigation test. reload widgets ? set widget in another container ?Contr
    }
}
