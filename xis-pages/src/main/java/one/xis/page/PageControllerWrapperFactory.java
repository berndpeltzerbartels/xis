package one.xis.page;

import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.controller.ControllerModelFactory;

@XISComponent
@RequiredArgsConstructor
class PageControllerWrapperFactory {

    private final ControllerModelFactory modelFactory;

    PageControllerWrapper createWrapper(Object controller) {
        var controllerModel = modelFactory.controllerModel(controller.getClass());
        return null;
    }
}
