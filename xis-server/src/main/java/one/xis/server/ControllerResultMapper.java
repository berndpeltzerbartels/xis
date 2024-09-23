package one.xis.server;

import one.xis.context.XISComponent;

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
        controllerResult.getWidgetParameters().putAll(controllerMethodResult.getWidgetParameters());
        controllerResult.getPathVariables().putAll(controllerMethodResult.getPathVariables());
        controllerResult.getUrlParameters().putAll(controllerMethodResult.getUrlParameters());
        controllerResult.getValidatorMessages().getGlobalMessages().addAll(controllerMethodResult.getValidatorMessages().getGlobalMessages());
        controllerResult.getValidatorMessages().getMessages().putAll(controllerMethodResult.getValidatorMessages().getMessages());
        if (controllerMethodResult.isValidationFailed()) {
            controllerResult.setValidationFailed(true);
        }
    }
}
