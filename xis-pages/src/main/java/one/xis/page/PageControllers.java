package one.xis.page;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.controller.ControllerWrapper;
import one.xis.controller.ControllerWrapperFactory;

import java.util.HashMap;
import java.util.Map;

@XISComponent
@RequiredArgsConstructor
class PageControllers {

    private final ControllerWrapperFactory wrapperFactory;
    private final Map<String, ControllerWrapper> controllerWrappers = new HashMap<>();

    void addControllerWrapper(Object pageController, PageMetaData metaData) {
        controllerWrappers.put(metaData.getPath(), wrapperFactory.createControllerWrapper(pageController, metaData.getControllerModel()));
    }

    ControllerWrapper getPageControllerWrapper(@NonNull String path) {
        return controllerWrappers.get(path);
    }
}
