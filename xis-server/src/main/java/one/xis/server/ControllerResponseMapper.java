package one.xis.server;

import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;

@XISComponent
@RequiredArgsConstructor
class ControllerResponseMapper {

    private final DataSerializer dataSerializer;
    private final PathResolver pathResolver;

    void mapResultToResponse(ServerResponse response, ControllerResult result) {
        if (result.getNextPageURL() != null) {
            var path = pathResolver.createPath(result.getNextPageURL());
            var pathString = pathResolver.evaluateRealPath(path, result.getPathVariables(), result.getUrlParameters());
            response.setNextPageURL(pathString);
        }
        response.setData(dataSerializer.serialize(result.getModelData()));

        response.setNextWidgetId(result.getNextWidgetId());
        response.setWidgetParameters(result.getWidgetParameters());
        response.setValidatorMessages(result.getValidatorMessages());
        response.setHttpStatus(result.isValidationFailed() ? 422 : 200);
    }

    private void mapValidatorMessages(ControllerResult result) {
        result.getValidatorMessages();


    }
}
