package one.xis.server;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.xis.Page;
import one.xis.context.Component;
import one.xis.context.Init;
import one.xis.context.Inject;
import one.xis.utils.http.HttpUtils;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
class PageControllerWrappers {

    private final ControllerWrapperFactory controllerWrapperFactory;
    private final PathResolver pathResolver;

    @Inject(annotatedWith = Page.class)
    private Collection<Object> pageControllers;

    @Getter
    private Collection<PageControllerEntry> pageControllerEntries;


    @Init
    void init() {
        pageControllerEntries = pageControllers.stream()
                .map(this::createEntry)
                .collect(Collectors.toSet());
    }


    Optional<ControllerWrapper> findByPath(String normalizedPath) {
        return pageControllerEntries.stream()
                .map(PageControllerEntry::getWrapper)
                .filter(wrapper -> wrapper.getId().equals(normalizedPath))
                .findFirst();
    }

    Optional<PageControllerMatch> findByRealPath(String realPath) {
        String path = stripQuery(realPath);
        for (PageControllerEntry entry : pageControllerEntries) {
            Optional<Map<String, String>> match = entry.getPageUrl().matches(path);
            if (match.isPresent()) {
                return match.map(vars -> new PageControllerMatch(entry.getWrapper(), vars, HttpUtils.parseQueryParameters(realPath)));
            }
        }
        return Optional.empty();
    }

    Optional<ControllerWrapper> findByClass(Class<?> controllerClass) {
        return pageControllerEntries.stream()
                .map(PageControllerEntry::getWrapper)
                .filter(wrapper -> wrapper.getControllerClass().equals(controllerClass))
                .findFirst();
    }

    /* ========================= FACTORY ========================= */

    private PageControllerEntry createEntry(Object controller) {
        PageControllerWrapper wrapper = createPageWrapper(controller);
        PageUrl pageUrl = createPageUrl(controller);

        log.info("url: {} -> controller: {}",
                wrapper.getId(),
                wrapper.getControllerClass().getSimpleName());

        return new PageControllerEntry(wrapper, pageUrl);
    }

    private PageControllerWrapper createPageWrapper(Object controller) {
        return (PageControllerWrapper) controllerWrapperFactory.createControllerWrapper(
                getPagePath(controller),
                controller,
                PageControllerWrapper.class
        );
    }

    private PageUrl createPageUrl(Object controller) {
        return new PageUrl(PageUtil.getUrl(controller.getClass()));
    }

    private String getPagePath(Object controller) {
        return pathResolver.normalizedPath(controller);
    }

    /* ========================= UTIL ========================= */

    private static String stripQuery(String realPath) {
        int idx = realPath.indexOf('?');
        return idx != -1 ? realPath.substring(0, idx) : realPath;
    }
}
