package one.xis.server;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.xis.Page;
import one.xis.context.XISComponent;
import one.xis.context.XISInit;
import one.xis.context.XISInject;
import one.xis.utils.http.HttpUtils;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@XISComponent
@RequiredArgsConstructor
class PageControllerWrappers {

    private final ControllerWrapperFactory controllerWrapperFactory;
    private final PathResolver pathResolver;

    @XISInject(annotatedWith = Page.class)
    private Collection<Object> pageControllers;

    @Getter
    private Collection<PageControllerEntry> pageControllerEntries;

    @XISInit
    void init() {
        pageControllerEntries = createEntries();
    }

    Optional<ControllerWrapper> findByPath(String normalizedPath) {
        return pageControllerEntries.stream()
                .map(PageControllerEntry::getWrapper)
                .filter(wrapper -> wrapper.getId().equals(normalizedPath))
                .findFirst();
    }

    Optional<PageControllerMatch> findByRealPath(String realPath) {
        int queryIndex = realPath.indexOf('?');
        String pathWithoutQuery =
                queryIndex != -1 ? realPath.substring(0, queryIndex) : realPath;

        for (var entry : pageControllerEntries) {
            var match = entry.getPageUrl().matches(pathWithoutQuery);
            if (match.isPresent()) {
                return match.map(vars ->
                        new PageControllerMatch(
                                entry.getWrapper(),
                                vars,
                                HttpUtils.parseQueryParameters(realPath)
                        )
                );
            }
        }
        return Optional.empty();
    }

    private Collection<PageControllerEntry> createEntries() {
        return pageControllers.stream()
                .map(controller -> {
                    PageControllerWrapper wrapper =
                            (PageControllerWrapper) createControllerWrapper(controller, this::getPagePath);

                    // ID ist z.B. /{xyz}/x.html
                    PageUrl pageUrl = new PageUrl(PageUtil.getUrl(controller.getClass()));

                    log.info("url: {} -> controller: {}",
                            wrapper.getId(),
                            wrapper.getControllerClass().getSimpleName());

                    return new PageControllerEntry(wrapper, pageUrl);
                })
                .collect(Collectors.toSet());
    }

    private ControllerWrapper createControllerWrapper(Object controller, Function<Object, String> idMapper) {
        return controllerWrapperFactory.createControllerWrapper(
                idMapper.apply(controller),
                controller,
                PageControllerWrapper.class
        );
    }

    private String getPagePath(Object pageController) {
        return pathResolver.normalizedPath(pageController);
    }

    Optional<ControllerWrapper> findByClass(Class<?> controllerClass) {
        return pageControllerEntries.stream()
                .map(PageControllerEntry::getWrapper)
                .filter(wrapper -> wrapper.getControllerClass().equals(controllerClass))
                .findFirst();
    }
}
