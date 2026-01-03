package one.xis.server;

import one.xis.context.Component;

import java.net.URLEncoder;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Map;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

@Component
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
                return e.getKey() + "=" + URLEncoder.encode(e.getValue().toString(), UTF_8);
            }).collect(Collectors.joining("&")));
        }
        return realPath.toString();
    }

    /**
     * Extracts path variables from a concrete path that matches a pattern.
     * For example, pattern "/product/{id}.html" and path "/product/123.html" 
     * returns map with "id" -> "123"
     * 
     * @param pattern the path pattern with variables
     * @param concretePath the concrete path to extract from
     * @return map of variable names to their values, or empty map if no match
     */
    Map<String, String> extractPathVariables(Path pattern, String concretePath) {
        var result = new java.util.HashMap<String, String>();
        if (concretePath == null || !pattern.matches(concretePath)) {
            return result;
        }
        extractPathVariablesRecursive(pattern.getPathElement(), concretePath, 0, result);
        return result;
    }

    private int extractPathVariablesRecursive(PathElement element, String path, int position, Map<String, String> result) {
        if (element == null) {
            return position;
        }

        if (element instanceof PathVariable) {
            PathVariable variable = (PathVariable) element;
            PathElement next = element.getNext();
            
            if (next == null) {
                // Last element is a variable - capture rest of path
                result.put(variable.getKey(), path.substring(position));
                return path.length();
            }
            
            if (next instanceof PathString) {
                String nextContent = ((PathString) next).getContent();
                int nextPos = path.indexOf(nextContent, position);
                if (nextPos == -1) {
                    return position;
                }
                // Extract the variable value
                String value = path.substring(position, nextPos);
                result.put(variable.getKey(), value);
                return extractPathVariablesRecursive(next, path, nextPos, result);
            }
        } else if (element instanceof PathString) {
            String content = ((PathString) element).getContent();
            if (path.startsWith(content, position)) {
                return extractPathVariablesRecursive(element.getNext(), path, position + content.length(), result);
            }
        }
        
        return position;
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
