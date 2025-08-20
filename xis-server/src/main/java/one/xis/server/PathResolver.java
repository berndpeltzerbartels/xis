package one.xis.server;

import one.xis.context.XISComponent;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Map;
import java.util.stream.Collectors;

@XISComponent
class PathResolver {

    Path createPath(String path) {
        return new Path(evaluate(new StringCharacterIterator(path)));
    }

    String normalizedPath(Object controller) {
        return normalizedPath(controller.getClass());
    }

    String normalizedPath(Class<?> controllerClass) {
        var path = createPath(PageUtil.getUrl(controllerClass));
        return path.normalized();
    }

    String evaluateRealPath(Path path, Map<String, Object> pathVariables, Map<String, Object> queryParameters) {
        StringBuilder realPath = new StringBuilder();
        var element = path.getPathElement();
        while (element != null) {
            realPath.append(element.evaluate(pathVariables));
            element = element.getNext();
        }
        if (!queryParameters.isEmpty()) {
            realPath.append("?").append(queryParameters.entrySet().stream().map(e -> {
                if (e.getValue() == null) {
                    return null;
                }
                try {
                    return e.getKey() + "=" + URLEncoder.encode(e.getValue().toString(), "UTF-8");
                } catch (UnsupportedEncodingException ex) {
                    throw new RuntimeException("unsupported encoding ", ex);
                }
            }).collect(Collectors.joining("&")));
        }
        return realPath.toString();
    }

    private PathElement evaluate(CharacterIterator path) {
        PathElement rv = null;
        if (path.current() != CharacterIterator.DONE) {
            var pathString = PathString.read(path);
            if (pathString != null) {
                rv = pathString;
                evaluatePathVariable(path, pathString);
            } else {
                var pathVariable = PathVariable.read(path);
                if (pathVariable != null) {
                    rv = pathVariable;
                    evaluatePathString(path, pathVariable);
                }
            }
        }
        return rv;
    }

    private void evaluatePathString(CharacterIterator path, PathVariable previous) {
        var pathString = PathString.read(path);
        if (pathString != null) {
            previous.setNext(pathString);
            evaluatePathVariable(path, pathString);
        }

    }

    private void evaluatePathVariable(CharacterIterator path, PathString previous) {
        var pathVariable = PathVariable.read(path);
        if (pathVariable != null) {
            previous.setNext(pathVariable);
            evaluatePathString(path, pathVariable);
        }

    }
}
