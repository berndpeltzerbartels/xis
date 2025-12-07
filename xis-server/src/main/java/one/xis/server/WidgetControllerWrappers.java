package one.xis.server;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.xis.Widget;
import one.xis.context.XISComponent;
import one.xis.context.XISInit;
import one.xis.context.XISInject;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@XISComponent
@RequiredArgsConstructor
class WidgetControllerWrappers {

    private final ControllerWrapperFactory controllerWrapperFactory;

    @XISInject(annotatedWith = Widget.class)
    private Collection<Object> widgetControllers;

    @XISInject
    private PathResolver pathResolver;

    @Getter
    private Collection<ControllerWrapper> widgetControllerWrappers;

    @XISInit
    void init() {
        widgetControllerWrappers = widgetControllerWrappers();
    }

    Optional<ControllerWrapper> findWidgetById(String widgetId) {
        return widgetControllerWrappers.stream()
                .filter(controller -> controller.getId().equals(widgetId))
                .findFirst();
    }

    Optional<ControllerWrapper> findByClass(@NonNull Class<?> cl) {
        return widgetControllerWrappers.stream().filter(c -> c.getControllerClass().equals(cl)).findFirst();
    }

    Collection<ControllerWrapper> getAllWidgets() {
        return widgetControllerWrappers;
    }

    Optional<ControllerWrapper> findWidgetByUrl(String url) {
        return widgetControllerWrappers.stream()
                .filter(wrapper -> {
                    var widgetUrl = WidgetUtil.getUrl(wrapper.getControllerClass());
                    return !widgetUrl.isEmpty() && widgetUrl.equals(url);
                })
                .findFirst();
    }


    Collection<DefaultWidget> findDefaultWidgetsByPageUrl(String url) {
        return widgetControllerWrappers.stream()
                .filter(wrapper -> matchesUrl(wrapper, url))
                .map(wrapper -> new DefaultWidget(wrapper.getId(), containerId(wrapper)))
                .toList();
    }

    private boolean matchesUrl(ControllerWrapper controllerWrapper, String url) {
        var widgetUrl = WidgetUtil.getUrl(controllerWrapper.getControllerClass());
        if (widgetUrl.isEmpty()) {
            return false;
        }
        var pattern = pathResolver.createPath(widgetUrl);
        return pattern.matches(url);
    }

    private String containerId(ControllerWrapper controllerWrapper) {
        String containerId = WidgetUtil.getContainerId(controllerWrapper.getControllerClass());
        if (containerId.isEmpty()) {
            throw new IllegalStateException("Widget " + controllerWrapper.getId() + " is linked to a path and does not have a containerId defined.");
        }
        return containerId;
    }

    private Collection<ControllerWrapper> widgetControllerWrappers() {
        return widgetControllers.stream()
                .map(controller -> createControllerWrapper(controller, WidgetUtil::getId))
                .peek(wrapper -> log.info("widget-id: {} -> controller: {}", wrapper.getId(), wrapper.getController().getClass().getSimpleName()))
                .collect(Collectors.toSet());
    }

    private ControllerWrapper createControllerWrapper(Object controller, Function<Object, String> idMapper) {
        return controllerWrapperFactory.createControllerWrapper(idMapper.apply(controller), controller, WidgetControllerWrapper.class);
    }

}
