package one.xis.widget;

import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.controller.ControllerModelFactory;

@XISComponent
@RequiredArgsConstructor
class WidgetControllerWrapperFactory {

    private final ControllerModelFactory modelFactory;

    WidgetControllerWrapper createWrapper(Object controller) {
        var model = modelFactory.controllerModel(controller.getClass());
        return null;
    }
}
