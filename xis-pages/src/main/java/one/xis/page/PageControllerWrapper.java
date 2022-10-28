package one.xis.page;

import one.xis.common.RequestContext;
import one.xis.controller.ControllerWrapper;


class PageControllerWrapper extends ControllerWrapper {

    public PageControllerWrapper(Object contoller, Class<?> modelType) {
        super(contoller, modelType);
    }

    Object invokeInit(RequestContext context) {
        return null;
    }

    Object createModelInstance() {
        return null;
    }


}
