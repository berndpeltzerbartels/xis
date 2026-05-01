package one.xis.server;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.xis.Frontlet;
import one.xis.context.Component;
import one.xis.context.Init;
import one.xis.context.Inject;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
class FrontletControllerWrappers {

    private final ControllerWrapperFactory controllerWrapperFactory;

    @Inject(annotatedWith = Frontlet.class)
    private Collection<Object> frontletControllers;

    @Inject
    private PathResolver pathResolver;

    @Getter
    private Collection<ControllerWrapper> frontletControllerWrappers;

    @Init
    void init() {
        frontletControllerWrappers = frontletControllerWrappers();
    }

    Optional<ControllerWrapper> findWidgetById(String widgetId) {
        return frontletControllerWrappers.stream()
                .filter(controller -> controller.getId().equals(widgetId))
                .findFirst();
    }

    Optional<ControllerWrapper> findByClass(@NonNull Class<?> cl) {
        return frontletControllerWrappers.stream().filter(c -> c.getControllerClass().equals(cl)).findFirst();
    }

    Collection<ControllerWrapper> getAllWidgets() {
        return frontletControllerWrappers;
    }

    Optional<ControllerWrapper> findWidgetByUrl(String url) {
        return frontletControllerWrappers.stream()
                .filter(wrapper -> {
                    var widgetUrl = FrontletUtil.getUrl(wrapper.getControllerClass());
                    return !widgetUrl.isEmpty() && widgetUrl.equals(url);
                })
                .findFirst();
    }

    Collection<DefaultWidget> findDefaultWidgetsByPageUrl(String url) {
        return frontletControllerWrappers.stream()
                .filter(wrapper -> matchesUrl(wrapper, url))
                .map(wrapper -> new DefaultWidget(wrapper.getId(), containerId(wrapper)))
                .toList();
    }

    private boolean matchesUrl(ControllerWrapper controllerWrapper, String url) {
        var widgetUrl = FrontletUtil.getUrl(controllerWrapper.getControllerClass());
        if (widgetUrl.isEmpty()) {
            return false;
        }
        var pattern = pathResolver.createPath(widgetUrl);
        return pattern.matches(url);
    }

    private String containerId(ControllerWrapper controllerWrapper) {
        String containerId = FrontletUtil.getContainerId(controllerWrapper.getControllerClass());
        if (containerId.isEmpty()) {
            throw new IllegalStateException("Frontlet " + controllerWrapper.getId() + " is linked to a path and does not have a containerId defined.");
        }
        return containerId;
    }

    private Collection<ControllerWrapper> frontletControllerWrappers() {
        return frontletControllers.stream()
                .map(controller -> createControllerWrapper(controller, FrontletUtil::getId))
                .peek(wrapper -> log.info("frontlet-id: {} -> controller: {}", wrapper.getId(), wrapper.getController().getClass().getSimpleName()))
                .collect(Collectors.toSet());
    }

    private ControllerWrapper createControllerWrapper(Object controller, Function<Object, String> idMapper) {
        return controllerWrapperFactory.createControllerWrapper(idMapper.apply(controller), controller, FrontletControllerWrapper.class);
    }
}
