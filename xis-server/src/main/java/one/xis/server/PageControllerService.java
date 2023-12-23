package one.xis.server;

import lombok.RequiredArgsConstructor;
import one.xis.PageResponse;
import one.xis.WidgetResponse;
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
        if (result.returnValue() == null || result.returnValue() == Void.class || result.returnValue().equals(invokerControllerWrapper.getControllerClass())) {
            return createPageResponse(invokerControllerWrapper.invokeGetModelMethods(request), invokerControllerWrapper);// Still the same controller
        } else if (result.returnValue() instanceof WidgetResponse widgetResponse) {
            if (widgetResponse.getTargetContainer() == null) { // TODO Client side code and test for this case
                throw new IllegalStateException(invokerControllerWrapper.getControllerClass().getSimpleName() + ": widget-result of a page-controller must define a target-container");
            }
            return processActionResult(request, widgetResponse);
        } else if (result.returnValue() instanceof PageResponse pageResponse) {
            return processPageResult(request, pageResponse);
        } else if (result.returnValue() instanceof Class<?> controllerClass) {
            return processActionResult(request, controllerClass);
        } else {
            throw new IllegalStateException(result.getClass() + " is not a valid return type for a widget-action");
        }
    }

    private ControllerWrapper findPageControllerWrapper(ClientRequest request) {
        return controllerWrappers.findByPath(request.getPageId()).orElseThrow();
    }

}
