package one.xis.server;

import lombok.RequiredArgsConstructor;
import one.xis.UserContext;
import one.xis.context.Component;

import java.util.Set;

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
        response.setReturnedFormDataKeys(result.getReturnedFormDataKeys());
        response.setNextFrontletId(result.getNextFrontletId());
        response.setNextModalId(result.getNextModalId());
        response.setCloseModal(result.isCloseModal());
        response.setReloadModalParent(result.isReloadModalParent());
        response.setValidatorMessages(result.getValidatorMessages());
        response.setFrontletContainerId(result.getFrontletContainerId());
        mapSecurityContext(response);
        response.getSessionStorageData().putAll(result.getSessionStorage());
        response.getLocalStorageData().putAll(result.getLocalStorage());
        response.getClientStateData().putAll(result.getClientState());
        response.getToastMessages().clear();
        response.getToastMessages().addAll(result.getToastMessages());
        response.setRedirectUrl(result.getRedirectUrl());
        response.getIdVariables().putAll(result.getIdVariables());
        response.getFrontletParameters().putAll(result.getFrontletParameters());
        response.getModalParameters().putAll(result.getModalParameters());
        if (result.getAnnotatedTitle() != null) {
            response.setAnnotatedTitle(result.getAnnotatedTitle());
        }
        if (result.getAnnotatedAddress() != null) {
            response.setAnnotatedAddress(result.getAnnotatedAddress());
        }
        if (response.getStatus() < 1)
            response.setStatus(result.isValidationFailed() ? 422 : 200);
        // TODO navigation test. reload frontlets ? set frontlet in another container ?Contr
    }

    private void mapSecurityContext(ServerResponse response) {
        var userContext = UserContext.getInstance();
        var authenticated = userContext.isAuthenticated();
        response.setAuthenticated(authenticated);
        response.setUserRoles(authenticated && userContext.getRoles() != null ? userContext.getRoles() : Set.of());
    }
}
