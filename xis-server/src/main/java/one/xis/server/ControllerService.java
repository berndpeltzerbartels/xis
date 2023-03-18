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

import static java.util.Collections.emptyMap;

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
        var data = findPageControllerWrapper(request).map(wrapper -> wrapper.invokeGetModelMethods(request)).orElse(emptyMap());
        return new Response(data, request.getControllerId(), null);
    }

    Response invokeWidgetModelMethods(Request request) {
        var data = findWidgetControllerWrapper(request).map(wrapper -> wrapper.invokeGetModelMethods(request)).orElse(emptyMap());
        return new Response(data, null, request.getControllerId());
    }

    Response invokePageActionMethod(Request request) {
        var invokerController = findPageControllerWrapper(request).orElseThrow();
        var result = invokerController.invokeActionMethod(request);
        var nextPageController = widgetControllerWrapperByResult(result).orElse(invokerController);
        return pageModelDataResponse(nextPageController, request);
    }

    Response invokeWidgetActionMethod(Request request) {
        var result = findPageControllerWrapper(request).orElseThrow().invokeActionMethod(request);
        return pageControllerWrapperByResult(result).map(wrapper -> pageModelDataResponse(wrapper, request))
                .orElseGet(() -> widgetControllerWrapperByResult(result).map(wrapper -> widgetModelDataResponse(wrapper, request)).orElseThrow());
    }

    private Response widgetModelDataResponse(ControllerWrapper wrapper, Request request) {
        return new Response(wrapper.invokeGetModelMethods(request), null, wrapper.getId());
    }

    private Response pageModelDataResponse(ControllerWrapper wrapper, Request request) {
        return new Response(wrapper.invokeGetModelMethods(request), wrapper.getId(), null);
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


    private Optional<ControllerWrapper> widgetControllerWrapperByResult(Object result) {
        if (result instanceof Class) {
            return Optional.of(controllerWrapperByClass((Class<?>) result, widgetControllerWrappers));
        } else if (result instanceof String) {
            return Optional.of(controllerWrapperById((String) result, widgetControllerWrappers));
        }
        return Optional.empty();
    }

    private Optional<ControllerWrapper> pageControllerWrapperByResult(Object result) {
        if (result instanceof Class) {
            return Optional.of(controllerWrapperByClass((Class<?>) result, pageControllerWrappers));
        } else if (result instanceof String) {
            return Optional.of(controllerWrapperById((String) result, pageControllerWrappers));
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
