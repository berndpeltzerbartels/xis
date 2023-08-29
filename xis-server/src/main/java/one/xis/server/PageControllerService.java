package one.xis.server;

import lombok.RequiredArgsConstructor;
import one.xis.PageResult;
import one.xis.WidgetResult;
import one.xis.context.XISComponent;

@XISComponent
@RequiredArgsConstructor
class PageControllerService extends ControllerService {

    private final PageControllerWrappers controllerWrappers;

    ServerResponse processPageModelDataRequest(ClientRequest request) {
        var wrapper = findPageControllerWrapper(request);
        return invokeGetPageModelMethods(200, wrapper, request);
    }

    ServerResponse processPageActionRequest(ClientRequest request) {
        var invokerControllerWrapper = pageControllerWrapperById(request.getPageId());
        var result = invokerControllerWrapper.invokeActionMethod(request);
        if (result == null || result == Void.class) {
            return invokeGetPageModelMethods(200, invokerControllerWrapper, request);// Still the same controller
        } else if (result instanceof WidgetResult widgetResult) {
            if (widgetResult.getTargetContainer() == null) { // TODO Client side code and test for this case
                throw new IllegalStateException(invokerControllerWrapper.getControllerClass().getSimpleName() + ": widget-result of a page-controller must define a target-container");
            }
            return processActionResult(request, widgetResult);
        } else if (result instanceof PageResult pageResult) {
            return processPageResult(request, pageResult);
        } else if (result instanceof Class<?> controllerClass) {
            return processActionResult(request, controllerClass);
        } else {
            throw new IllegalStateException(result.getClass() + " is not a valid return type for a widget-action");
        }
    }

    private ControllerWrapper findPageControllerWrapper(ClientRequest request) {
        return controllerWrappers.findByPath(request.getPageId()).orElseThrow();
    }

}
