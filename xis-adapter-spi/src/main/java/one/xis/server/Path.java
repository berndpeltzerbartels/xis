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
