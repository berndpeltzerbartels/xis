package one.xis.server;

import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;

@XISComponent
@RequiredArgsConstructor
class ControllerResponseMapper {

    private final PathResolver pathResolver;

    void mapResultToResponse(ServerResponse response, ControllerResult result) {
        if (result.getNextPageURL() != null) {
            var path = pathResolver.createPath(result.getNextPageURL());
            var pathString = pathResolver.evaluateRealPath(path, result.getPathVariables(), result.getUrlParameters());
            response.setNextPageURL(pathString);
        }
        response.setData(result.getModelData());
        response.setFormData(result.getFormData());
        response.setNextWidgetId(result.getNextWidgetId());
        response.setValidatorMessages(result.getValidatorMessages());
        response.setStatus(result.isValidationFailed() ? 422 : 200);
        response.setWidgetContainerId(result.getWidgetContainerId());
        response.setLocalStorageData(result.getLocalStorage());
        response.setClientScopeData(result.getClientScope());
        response.setReloadWidgets(result.getWidgetsToReload());
        response.setClientStateData(result.getClientState());
        // TODO navigation test. reload widgets ? set widget in another container ?
    }
}
