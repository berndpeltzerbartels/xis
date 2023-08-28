package one.xis.server;

import lombok.RequiredArgsConstructor;
import one.xis.PageResult;
import one.xis.WidgetResult;
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
        if (result == null || result == Void.class) {
            return invokeGetWidgetModelMethods(200, invokerControllerWrapper, request);// Still the same controller
        } else if (result instanceof WidgetResult widgetResult) {
            return processActionResult(request, widgetResult);
        } else if (result instanceof PageResult pageResult) {
            return processActionResult(request, pageResult);
        } else if (result instanceof Class<?> controllerClass) {
            return processActionResult(request, controllerClass);
        } else {
            throw new IllegalStateException(result.getClass() + " is not a valid return type for a widget-action");
        }
    }
}
