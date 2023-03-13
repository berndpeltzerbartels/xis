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

    private final ControllerWrapperFactory controllerWrapperFactory;

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
    

    Response invokePageModelMethods(Request request) {
        var data = findPageControllerWrapper(request).orElseThrow().invokeGetModelMethods(request);
        return new Response(data, request.getControllerId());
    }

    Response invokeWidgetModelMethods(Request request) {
        var data = findWidgetControllerWrapper(request).orElseThrow().invokeGetModelMethods(request);
        return new Response(data, request.getControllerId());
    }

    Response invokePageActionMethod(Request request) {
        var result = findPageControllerWrapper(request).orElseThrow().invokeActionMethod(request);
        var next = controllerWrapperByResult(result, pageControllerWrappers).orElseThrow();
        return new Response(next.invokeGetModelMethods(request), next.getId());
    }

    Response invokeWidgetActionMethod(Request request) {
        var result = findPageControllerWrapper(request).orElseThrow().invokeActionMethod(request);
        var next = controllerWrapperByResult(result, widgetControllerWrappers).orElseThrow();
        return new Response(next.invokeGetModelMethods(request), next.getId());
    }

    private Collection<ControllerWrapper> widgetControllerWrappers() {
        return widgetControllers.stream()
                .map(controller -> createControllerWrapper(controller, WidgetUtil::getId))
                .collect(Collectors.toSet());
    }

    private Collection<ControllerWrapper> pageControllerWrappers() {
        return pageControllers.stream()
                .map(controller -> createControllerWrapper(controller, this::getPagePath))
                .collect(Collectors.toSet());
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

    private ControllerWrapper createControllerWrapper(Object controller, Function<Object, String> idMapper) {
        return controllerWrapperFactory.createController(idMapper.apply(controller), controller);
    }


    private String getPagePath(Object pageController) {
        var path = pageController.getClass().getAnnotation(Page.class).path();
        if (!path.endsWith(".html")) {
            throw new IllegalStateException(pageController.getClass() + ": Identifier in @Page-annotation must have suffix '.html'");
        }
        return path;

    }

    private Optional<ControllerWrapper> findPageControllerWrapper(Request request) {
        return pageControllerWrappers.stream()
                .filter(controller -> controller.getId().equals(request.getControllerId()))
                .findFirst();
    }

    private Optional<ControllerWrapper> findWidgetControllerWrapper(Request request) {
        return widgetControllerWrappers.stream()
                .filter(controller -> controller.getId().equals(request.getControllerId()))
                .findFirst();
    }
}
