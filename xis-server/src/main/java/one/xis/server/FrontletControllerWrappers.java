package one.xis.server;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.xis.Frontlet;
import one.xis.Modal;
import one.xis.context.Component;
import one.xis.context.Init;
import one.xis.context.Inject;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
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

    @Inject(annotatedWith = Modal.class)
    private Collection<Object> modalControllers;

    @Inject
    private PathResolver pathResolver;

    @Getter
    private Collection<ControllerWrapper> frontletControllerWrappers;

    @Init
    void init() {
        frontletControllerWrappers = frontletControllerWrappers();
    }

    Optional<ControllerWrapper> findFrontletById(String frontletId) {
        return frontletControllerWrappers.stream()
                .filter(controller -> controller.getId().equals(frontletId))
                .findFirst();
    }

    Optional<ControllerWrapper> findByClass(@NonNull Class<?> cl) {
        return frontletControllerWrappers.stream().filter(c -> c.getControllerClass().equals(cl)).findFirst();
    }

    Collection<ControllerWrapper> getAllFrontlets() {
        return frontletControllerWrappers;
    }

    Optional<ControllerWrapper> findFrontletByUrl(String url) {
        return frontletControllerWrappers.stream()
                .filter(wrapper -> {
                    var frontletUrl = FrontletUtil.getUrl(wrapper.getControllerClass());
                    return !frontletUrl.isEmpty() && frontletUrl.equals(url);
                })
                .findFirst();
    }

    Optional<FrontletControllerMatch> findFrontletByRealPath(String url) {
        var pathOnly = stripQuery(url);
        for (ControllerWrapper wrapper : frontletControllerWrappers) {
            var frontletUrl = FrontletUtil.getUrl(wrapper.getControllerClass());
            if (frontletUrl.isEmpty()) {
                continue;
            }
            var pattern = pathResolver.createPath(frontletUrl);
            if (pattern.matches(pathOnly)) {
                var pathVariables = extractPathVariables(pattern, pathOnly);
                return Optional.of(new FrontletControllerMatch(wrapper, pathVariables));
            }
        }
        return Optional.empty();
    }

    Collection<DefaultFrontlet> findDefaultFrontletsByPageUrl(String url) {
        return frontletControllerWrappers.stream()
                .filter(wrapper -> matchesUrl(wrapper, url))
                .map(wrapper -> new DefaultFrontlet(wrapper.getId(), containerId(wrapper)))
                .toList();
    }

    private boolean matchesUrl(ControllerWrapper controllerWrapper, String url) {
        var frontletUrl = FrontletUtil.getUrl(controllerWrapper.getControllerClass());
        if (frontletUrl.isEmpty()) {
            return false;
        }
        var pattern = pathResolver.createPath(frontletUrl);
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
        var controllers = new java.util.ArrayList<Object>();
        controllers.addAll(frontletControllers);
        controllers.addAll(modalControllers);
        return controllers.stream()
                .map(controller -> createControllerWrapper(controller, FrontletUtil::getId))
                .peek(wrapper -> log.info("frontlet-id: {} -> controller: {}", wrapper.getId(), wrapper.getController().getClass().getSimpleName()))
                .collect(Collectors.toSet());
    }

    private String stripQuery(String url) {
        var index = url.indexOf('?');
        return index < 0 ? url : url.substring(0, index);
    }

    private Map<String, String> extractPathVariables(Path pattern, String path) {
        var values = new HashMap<String, String>();
        extractPathVariables(pattern.getPathElement(), path, 0, values);
        return values;
    }

    private int extractPathVariables(PathElement element, String path, int position, Map<String, String> values) {
        if (element == null) {
            return position;
        }
        if (element instanceof PathString pathString) {
            return extractPathVariables(element.getNext(), path, position + pathString.getContent().length(), values);
        }
        if (element instanceof PathVariable pathVariable) {
            var next = element.getNext();
            int end = path.length();
            if (next instanceof PathString nextString) {
                end = path.indexOf(nextString.getContent(), position);
            }
            values.put(pathVariable.getKey(), path.substring(position, end));
            return extractPathVariables(next, path, end, values);
        }
        return position;
    }

    private ControllerWrapper createControllerWrapper(Object controller, Function<Object, String> idMapper) {
        return controllerWrapperFactory.createControllerWrapper(idMapper.apply(controller), controller, FrontletControllerWrapper.class);
    }

    record FrontletControllerMatch(ControllerWrapper frontletControllerWrapper, Map<String, String> pathVariables) {
    }
}
