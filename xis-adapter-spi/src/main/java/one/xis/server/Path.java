package one.xis.server;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a path including url-parameters.
 */
@Data
class Path {
    private final PathElement pathElement;

    List<PathElement> getPathElements() {
        var rv = new ArrayList<PathElement>();
        var element = pathElement;
        while (element != null) {
            rv.add(element);
            element = element.getNext();
        }
        return rv;
    }

    boolean hasPathVariables() {
        var element = pathElement;
        while (element != null) {
            if (element instanceof one.xis.server.PathVariable) {
                return true;
            }
            element = element.getNext();
        }
        return false;
    }

    /**
     * Checks if this path pattern matches the given concrete path.
     * For example, pattern "/product/{id}.html" matches "/product/123.html"
     * 
     * @param concretePath the concrete path to check
     * @return true if the pattern matches the concrete path
     */
    boolean matches(String concretePath) {
        if (concretePath == null) {
            return false;
        }
        return matchesRecursive(pathElement, concretePath, 0);
    }

    private boolean matchesRecursive(PathElement element, String path, int position) {
        if (element == null) {
            // Pattern exhausted - match only if path is also exhausted
            return position == path.length();
        }

        if (element instanceof PathVariable) {
            // Path variable matches until next static part or end of path
            PathElement next = element.getNext();
            if (next == null) {
                // Last element is a variable - it matches the rest of the path
                return position < path.length();
            }
            // Find where the next static part starts
            if (next instanceof PathString) {
                String nextContent = ((PathString) next).getContent();
                int nextPos = path.indexOf(nextContent, position);
                if (nextPos == -1) {
                    return false;
                }
                // Check that we matched at least one character for the variable
                if (nextPos == position) {
                    return false;
                }
                return matchesRecursive(next, path, nextPos);
            }
            return false;
        } else if (element instanceof PathString) {
            String content = ((PathString) element).getContent();
            if (!path.startsWith(content, position)) {
                return false;
            }
            return matchesRecursive(element.getNext(), path, position + content.length());
        }
        
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Path)) {
            throw new IllegalArgumentException();
        }
        return Objects.equals(normalized(), ((Path) o).normalized());
    }

    @Override
    public int hashCode() {
        return normalized().hashCode();
    }

    @Override
    public String toString() {
        return normalized();
    }

    String normalized() {
        StringBuilder path = new StringBuilder();
        var element = pathElement;
        while (element != null) {
            path.append(element.normalized());
            element = element.getNext();
        }
        return path.toString();
    }
}
