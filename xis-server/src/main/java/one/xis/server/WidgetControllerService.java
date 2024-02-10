package one.xis.server;

import lombok.RequiredArgsConstructor;
import one.xis.PageResponse;
import one.xis.WidgetResponse;
import one.xis.context.XISComponent;

@XISComponent
@RequiredArgsConstructor
class WidgetControllerService extends ControllerService {

    void processWidgetModelDataRequest(ClientRequest request, ServerResponse response) {
        var wrapper = widgetControllerWrapperById(request.getWidgetId());
        invokeGetWidgetModelMethods(wrapper, request, response);
        response.setHttpStatus(200);
    }

    void processWidgetActionRequest(ClientRequest request, ServerResponse response) {
        var invokerControllerWrapper = widgetControllerWrapperById(request.getWidgetId());
        var result = invokerControllerWrapper.invokeActionMethod(request);
        if (result.returnValue() == null || result.returnValue() == Void.class || result.returnValue().equals(invokerControllerWrapper.getControllerClass())) {
            updateWidgetResponse(response, invokerControllerWrapper.invokeGetModelMethods(request), invokerControllerWrapper);// Still the same controller
        } else if (result.returnValue() instanceof WidgetResponse widgetResponse) {
            processActionResult(request, response, widgetResponse);
        } else if (result.returnValue() instanceof PageResponse pageResponse) {
            processActionResult(request, response, pageResponse);
        } else if (result.returnValue() instanceof Class<?> controllerClass) {
            processActionResult(request, response, controllerClass);
        } else {
            throw new IllegalStateException(result.getClass() + " is not a valid return type for a widget-action");
        }
    }
}
