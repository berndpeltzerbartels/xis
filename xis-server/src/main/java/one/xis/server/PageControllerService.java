package one.xis.server;

import lombok.RequiredArgsConstructor;
import one.xis.PageResponse;
import one.xis.WidgetResponse;
import one.xis.context.XISComponent;

@XISComponent
@RequiredArgsConstructor
class PageControllerService extends ControllerService {

    private final PageControllerWrappers controllerWrappers;

    void processPageModelDataRequest(ClientRequest request, ServerResponse response) {
        var wrapper = findPageControllerWrapper(request);
        invokeGetPageModelMethods(200, wrapper, request, response);
    }

    void processPageActionRequest(ClientRequest request, ServerResponse response) {
        var invokerControllerWrapper = pageControllerWrapperById(request.getPageId());
        var result = invokerControllerWrapper.invokeActionMethod(request);
        if (result.returnValue() == null || result.returnValue() == Void.class || result.returnValue().equals(invokerControllerWrapper.getControllerClass())) {
            var dataMap = invokerControllerWrapper.invokeGetModelMethods(request);// Still the same controller
            updatePageResponse(response, dataMap, invokerControllerWrapper);// Still the same controller
        } else if (result.returnValue() instanceof WidgetResponse widgetResponse) {
            if (widgetResponse.getTargetContainer() == null) { // TODO Client side code and test for this case
                throw new IllegalStateException(invokerControllerWrapper.getControllerClass().getSimpleName() + ": widget-result of a page-controller must define a target-container");
            }
            processActionResult(request, response, widgetResponse);
        } else if (result.returnValue() instanceof PageResponse pageResponse) {
            processPageResult(request, response, pageResponse);
        } else if (result.returnValue() instanceof Class<?> controllerClass) {
            processActionResult(request, response, controllerClass);
        } else {
            throw new IllegalStateException(result.getClass() + " is not a valid return type for a widget-action");
        }
    }

    private ControllerWrapper findPageControllerWrapper(ClientRequest request) {
        return controllerWrappers.findByPath(request.getPageId()).orElseThrow();
    }

}
