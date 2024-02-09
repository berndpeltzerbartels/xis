package one.xis.parameter;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
class PathElement {

    private final String name;
    private final int index;

    @Getter
    private final PathElement parent;
    private final Map<String, List<PathElement>> children = new HashMap<>();

    PathElement addChild(String name) {
        var path = new PathElement(name, getIndex(name), this);
        children.computeIfAbsent(name, k -> new ArrayList<>()).add(path);
        return path;
    }

    String toPathString() {
        var elements = new ArrayList<PathElement>();
        var p = this;
        while (p != null) {
            elements.add(p);
            p = p.parent;
        }
        Collections.reverse(elements);
        return elements.stream()
                .map(PathElement::elementName)
                .collect(Collectors.joining("/"));
    }

    void clearChildren() {
        children.clear();
    }


    protected String elementName() {
        return name + "[" + index + "]";
    }


    private int getIndex(String name) {
        var other = children.get(name);
        return other == null ? 0 : other.size();
    }

}
