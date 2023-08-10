package one.xis.server;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.xis.Page;
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
class ControllerService {

    private final ControllerWrapperFactory controllerWrapperFactory;
    private final PathResolver pathResolver;


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


    ServerResponse invokePageModelMethods(ClientRequest request) {
        var wrapper = findPageControllerWrapper(request).orElseThrow();
        return pageModelDataResponse(200, wrapper, request);
    }

    ServerResponse invokeWidgetModelMethods(ClientRequest request) {
        var wrapper = findWidgetControllerWrapper(request).orElseThrow();
        return widgetModelDataResponse(200, wrapper, request);
    }

    ServerResponse invokePageActionMethod(ClientRequest request) {
        var invokerController = findPageControllerWrapper(request).orElseThrow();
        var result = invokerController.invokeActionMethod(request);
        var nextPageController = pageControllerWrapperByResult(result).orElse(invokerController);
        return pageModelDataResponse(200, nextPageController, request);
    }

    ServerResponse invokeWidgetActionMethod(ClientRequest request) {
        var invokerController = findWidgetControllerWrapper(request).orElseThrow();
        var result = invokerController.invokeActionMethod(request);
        if (result == null || result == Void.class) {
            return widgetModelDataResponse(200, invokerController, request);
        }
        var controllerWrapper = widgetControllerWrapperByResult(result)
                .orElseGet(() -> pageControllerWrapperByResult(result).orElseThrow());
        if (controllerWrapper.getController().getClass().isAnnotationPresent(Widget.class)) {
            return widgetModelDataResponse(200, controllerWrapper, request);
        } else if (controllerWrapper.getController().getClass().isAnnotationPresent(Page.class)) {
            return pageModelDataResponse(200, controllerWrapper, request);
        } else {
            throw new IllegalStateException("not a controller: " + controllerWrapper.getClass());
        }

    }

    private ServerResponse widgetModelDataResponse(int status, ControllerWrapper wrapper, ClientRequest request) {
        return new ServerResponse(status, wrapper.invokeGetModelMethods(request), null, wrapper.getId());
    }

    private ServerResponse pageModelDataResponse(int status, ControllerWrapper wrapper, ClientRequest request) {
        return new ServerResponse(status, wrapper.invokeGetModelMethods(request), wrapper.getId(), null);
    }

    private Collection<ControllerWrapper> widgetControllerWrappers() {
        return widgetControllers.stream()
                .map(controller -> createControllerWrapper(controller, WidgetUtil::getId))
                .peek(wrapper -> log.info("widget-id: {} -> controller: {}", wrapper.getId(), wrapper.getController().getClass().getSimpleName()))
                .collect(Collectors.toSet());
    }

    private Collection<ControllerWrapper> pageControllerWrappers() {
        return pageControllers.stream()
                .map(controller -> createControllerWrapper(controller, this::getPagePath))
                .peek(wrapper -> log.info("url: {} -> controller: {}", wrapper.getId(), wrapper.getController().getClass().getSimpleName()))
                .collect(Collectors.toSet());
    }

    private Optional<ControllerWrapper> widgetControllerWrapperByResult(Object result) {
        if (result instanceof Class) {
            return controllerWrapperByClass((Class<?>) result, widgetControllerWrappers);
        } else if (result instanceof String) {
            return controllerWrapperById((String) result, widgetControllerWrappers);
        }
        return Optional.empty();
    }

    private Optional<ControllerWrapper> pageControllerWrapperByResult(Object result) {
        if (result instanceof Class) {
            return controllerWrapperByClass((Class<?>) result, pageControllerWrappers);
        } else if (result instanceof String) {
            return controllerWrapperById((String) result, pageControllerWrappers);
        }
        return Optional.empty();
    }

    private Optional<ControllerWrapper> controllerWrapperByClass(@NonNull Class<?> cl, Collection<ControllerWrapper> controllerWrappers) {
        return controllerWrappers.stream().filter(c -> c.getControllerClass().equals(cl)).findFirst();
    }

    private Optional<ControllerWrapper> controllerWrapperById(@NonNull String id, Collection<ControllerWrapper> controllerWrappers) {
        return controllerWrappers.stream().filter(c -> c.getId().equals(id)).findFirst();
    }

    private ControllerWrapper createControllerWrapper(Object controller, Function<Object, String> idMapper) {
        return controllerWrapperFactory.createControllerWrapper(idMapper.apply(controller), controller);
    }


    private String getPagePath(Object pageController) {
        var path = pageController.getClass().getAnnotation(Page.class).value();
        if (!path.endsWith(".html")) {
            throw new IllegalStateException(pageController.getClass() + ": Identifier in @Page-annotation must have suffix '.html'");
        }
        return pathResolver.normalizedPath(pageController);

    }

    private Optional<ControllerWrapper> findPageControllerWrapper(ClientRequest request) {
        return pageControllerWrappers.stream()
                .filter(controller -> controller.getId().equals(request.getPageId()))
                .findFirst();
    }

    private Optional<ControllerWrapper> findWidgetControllerWrapper(ClientRequest request) {
        return widgetControllerWrappers.stream()
                .filter(controller -> controller.getId().equals(request.getWidgetId()))
                .findFirst();
    }
}
