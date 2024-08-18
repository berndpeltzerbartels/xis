package one.xis.server;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.xis.Page;
import one.xis.context.XISComponent;
import one.xis.context.XISInit;
import one.xis.context.XISInject;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j // TODO May this doese not produce any logs for micronaut
@XISComponent
@RequiredArgsConstructor
class PageControllerWrappers {

    private final ControllerWrapperFactory controllerWrapperFactory;
    private final PathResolver pathResolver;

    @XISInject(annotatedWith = Page.class)
    private Collection<Object> pageControllers;


    @Getter
    private Collection<ControllerWrapper> pageControllerWrappers;

    @XISInit
    void init() {
        pageControllerWrappers = pageControllerWrappers();
    }

    Optional<ControllerWrapper> findByPath(Path path) {
        return pageControllerWrappers.stream()
                .filter(controller -> controller.getId().equals(path.normalized()))
                .findFirst();
    }

    private Collection<ControllerWrapper> pageControllerWrappers() {
        return pageControllers.stream()
                .map(controller -> createControllerWrapper(controller, this::getPagePath))
                .peek(wrapper -> log.info("url: {} -> controller: {}", wrapper.getId(), wrapper.getController().getClass().getSimpleName()))
                .collect(Collectors.toSet());
    }

    private ControllerWrapper createControllerWrapper(Object controller, Function<Object, String> idMapper) {
        return controllerWrapperFactory.createControllerWrapper(idMapper.apply(controller), controller, PageControllerWrapper.class);
    }

    private String getPagePath(Object pageController) {
        return pathResolver.normalizedPath(pageController);
    }

}
