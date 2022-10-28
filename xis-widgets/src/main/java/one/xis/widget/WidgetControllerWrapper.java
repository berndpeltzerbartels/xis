package one.xis.widget;

import one.xis.common.RequestContext;
import one.xis.controller.ControllerModel;
import one.xis.controller.ControllerWrapper;

class WidgetControllerWrapper extends ControllerWrapper {

    WidgetControllerWrapper(Object contoller, ControllerModel controllerModel, Class<?> modelType) {
        super(contoller, controllerModel, modelType);
    }

    Object invokeInit(RequestContext context) {
        return null;
    }
}
