package one.xis.page;

import one.xis.controller.ControllerModel;
import one.xis.controller.ControllerWrapper;


class PageControllerWrapper extends ControllerWrapper {

    PageControllerWrapper(Object contoller, ControllerModel controllerModel, Class<?> modelType) {
        super(contoller, controllerModel, modelType);
    }


    Object createModelInstance() {
        return null;
    }


}
