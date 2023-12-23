package one.xis.server;

import lombok.RequiredArgsConstructor;
import one.xis.PageResponse;
import one.xis.WidgetResponse;
import one.xis.context.XISComponent;

@XISComponent
@RequiredArgsConstructor
class WidgetControllerService extends ControllerService {

    ServerResponse processWidgetModelDataRequest(ClientRequest request) {
        var wrapper = widgetControllerWrapperById(request.getWidgetId());
        return invokeGetWidgetModelMethods(200, wrapper, request);
    }

    ServerResponse processWidgetActionRequest(ClientRequest request) {
        var invokerControllerWrapper = widgetControllerWrapperById(request.getWidgetId());
        var result = invokerControllerWrapper.invokeActionMethod(request);
        if (result.returnValue() == null || result.returnValue() == Void.class || result.returnValue().equals(invokerControllerWrapper.getControllerClass())) {
            return createWidgetResponse(invokerControllerWrapper.invokeGetModelMethods(request), invokerControllerWrapper);// Still the same controller
        } else if (result.returnValue() instanceof WidgetResponse widgetResponse) {
            return processActionResult(request, widgetResponse);
        } else if (result.returnValue() instanceof PageResponse pageResponse) {
            return processActionResult(request, pageResponse);
        } else if (result.returnValue() instanceof Class<?> controllerClass) {
            return processActionResult(request, controllerClass);
        } else {
            throw new IllegalStateException(result.getClass() + " is not a valid return type for a widget-action");
        }
    }
}
