package one.xis.server;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import one.xis.Page;
import one.xis.Widget;
import one.xis.context.XISComponent;
import one.xis.context.XISInit;
import one.xis.context.XISInject;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@XISComponent
@RequiredArgsConstructor
class ControllerService {

    private final ControllerFactory controllerFactory;

    @XISInject(annotatedWith = Widget.class)
    private Collection<Object> widgetControllers;

    @XISInject(annotatedWith = Page.class)
    private Collection<Object> pageControllers;

    @Getter
    private Collection<ControllerWrapper> widgetControllerWrappers;

    @Getter
    private Collection<ControllerWrapper> pageControllerWrappers;

    @XISInit
    void init() {
        widgetControllerWrappers = widgetControllerWrappers();
        pageControllerWrappers = pageControllerWrappers();
    }

    private Collection<ControllerWrapper> widgetControllerWrappers() {
        return widgetControllers.stream()
                .map(controller -> createController(controller, this::getWidgetId))
                .collect(Collectors.toSet());
    }

    private Collection<ControllerWrapper> pageControllerWrappers() {
        return pageControllers.stream()
                .map(controller -> createController(controller, this::getPagePath))
                .collect(Collectors.toSet());
    }

    Optional<ControllerWrapper> widgetControllerWrapperByResult(Object result) {
        return controllerWrapperByResult(result, widgetControllerWrappers);
    }

    Optional<ControllerWrapper> pageControllerWrapperByResult(Object result) {
        return controllerWrapperByResult(result, pageControllerWrappers);
    }

    private Optional<ControllerWrapper> controllerWrapperByResult(Object result, Collection<ControllerWrapper> controllerWrappers) {
        if (result instanceof Class) {
            return Optional.of(controllerWrapperByClass((Class<?>) result, controllerWrappers));
        } else if (result instanceof String) {
            return Optional.of(controllerWrapperById((String) result, controllerWrappers));
        }
        return Optional.empty();
    }

    private ControllerWrapper controllerWrapperByClass(@NonNull Class<?> cl, Collection<ControllerWrapper> controllerWrappers) {
        return controllerWrappers.stream().filter(c -> c.getControllerClass().equals(cl)).findFirst().orElseThrow();
    }

    private ControllerWrapper controllerWrapperById(@NonNull String id, Collection<ControllerWrapper> controllerWrappers) {
        return controllerWrappers.stream().filter(c -> c.getId().equals(id)).findFirst().orElseThrow();
    }

    private ControllerWrapper createController(Object controller, Function<Object, String> idMapper) {
        return controllerFactory.createController(idMapper.apply(controller), controller);
    }

    private String getWidgetId(Object widgetController) {
        return widgetController.getClass().getAnnotation(Widget.class).value();
    }

    private String getPagePath(Object widgetController) {
        return widgetController.getClass().getAnnotation(Page.class).path();
    }

}
