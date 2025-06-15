package one.xis.server;

import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;

@XISComponent
@RequiredArgsConstructor
class ControllerResponseMapper {

    private final PathResolver pathResolver;

    void mapResultToResponse(ServerResponse response, ControllerResult result) {
        response.setNextURL(result.getNextURL());
        response.setData(result.getModelData());
        response.setFormData(result.getFormData());
        response.setNextWidgetId(result.getNextWidgetId());
        response.setValidatorMessages(result.getValidatorMessages());
        response.setStatus(result.isValidationFailed() ? 422 : 200);
        response.setWidgetContainerId(result.getWidgetContainerId());
        response.setReloadWidgets(result.getWidgetsToReload());
        response.getClientStateData().putAll(result.getClientState());
        response.getLocalStorageData().putAll(result.getLocalStorage());
        response.setTokens(result.getTokens());
        // TODO navigation test. reload widgets ? set widget in another container ?
    }
}
