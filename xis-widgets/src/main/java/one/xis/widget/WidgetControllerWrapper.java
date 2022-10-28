package one.xis.widget;

import one.xis.common.RequestContext;
import one.xis.controller.ControllerWrapper;

class WidgetControllerWrapper extends ControllerWrapper {

    WidgetControllerWrapper(Object contoller, Class<?> modelType) {
        super(contoller, modelType);
    }

    Object invokeInit(RequestContext context) {
        return null;
    }
}
