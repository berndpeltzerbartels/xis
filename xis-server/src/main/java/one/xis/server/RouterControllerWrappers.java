package one.xis.server;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.xis.Router;
import one.xis.context.Component;
import one.xis.context.Init;
import one.xis.context.Inject;
import one.xis.utils.http.HttpUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
class RouterControllerWrappers {

    private final ControllerWrapperFactory controllerWrapperFactory;
    private final PathResolver pathResolver;

    @Inject(annotatedWith = Router.class)
    private Collection<Object> routerControllers;

    @Getter
    private Collection<RouterControllerEntry> routerControllerEntries;

    @Init
    void init() {
        routerControllerEntries = routerControllers.stream()
                .flatMap(controller -> createEntries(controller).stream())
                .collect(Collectors.toSet());
    }

    Optional<RouterControllerMatch> findByRealPath(String realPath) {
        var path = stripQuery(realPath);
        for (RouterControllerEntry entry : routerControllerEntries) {
            Optional<Map<String, String>> match = entry.pageUrl().matches(path);
            if (match.isPresent()) {
                return match.map(vars -> new RouterControllerMatch(entry.wrapper(), entry.method(), vars, HttpUtils.parseQueryParameters(realPath)));
            }
        }
        return Optional.empty();
    }

    Collection<String> normalizedPaths() {
        return routerControllerEntries.stream()
                .map(entry -> entry.path().normalized())
                .collect(Collectors.toSet());
    }

    private Collection<RouterControllerEntry> createEntries(Object controller) {
        var wrapper = controllerWrapperFactory.createRouterControllerWrapper(controller);
        var entries = new ArrayList<RouterControllerEntry>();
        for (Map.Entry<String, ControllerMethod> entry : wrapper.getRouteMethods().entrySet()) {
            var routeUrl = entry.getKey();
            var path = pathResolver.createPath(routeUrl);
            log.info("router-url: {} -> controller: {}#{}",
                    path.normalized(),
                    wrapper.getControllerClass().getSimpleName(),
                    entry.getValue().getMethod().getName());
            entries.add(new RouterControllerEntry(wrapper, entry.getValue(), path, new PageUrl(routeUrl)));
        }
        return entries;
    }

    private String stripQuery(String realPath) {
        int idx = realPath.indexOf('?');
        return idx != -1 ? realPath.substring(0, idx) : realPath;
    }

    record RouterControllerEntry(RouterControllerWrapper wrapper, ControllerMethod method, Path path, PageUrl pageUrl) {
    }

    record RouterControllerMatch(RouterControllerWrapper wrapper,
                                 ControllerMethod method,
                                 Map<String, String> pathVariables,
                                 Map<String, String> queryParameters) {
    }
}
