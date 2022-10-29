package one.xis.widget;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.controller.ControllerWrapper;
import one.xis.controller.ControllerWrapperFactory;

import java.util.HashMap;
import java.util.Map;

@XISComponent
@RequiredArgsConstructor
class WidgetControllers {

    private final ControllerWrapperFactory wrapperFactory;
    private final Map<String, ControllerWrapper> controllerWrappers = new HashMap<>();

    void addWidgetController(Object pageController, WidgetMetaData metaData) {
        controllerWrappers.put(metaData.getId(), wrapperFactory.createControllerWrapper(pageController, metaData.getControllerModel()));
    }

    ControllerWrapper getWidgetControllerWrapper(@NonNull String id) {
        return controllerWrappers.get(id);
    }
}
