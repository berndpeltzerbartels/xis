package one.xis.validation;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
public class DefaultPathElement implements PathElement {

    private final String name;
    private final String path;
    private final Map<String, PathElement> children = new HashMap<>();

    public DefaultPathElement(DefaultPathElement parent) {
        this(parent.getName(), parent.getPath());
    }

    public DefaultPathElement addChild(String name) {
        if (children.containsKey(name)) {
            throw new IllegalArgumentException("Child with name " + name + " already exists");
        }
        var pathElement = new DefaultPathElement(name, path + "/" + name);
        children.put(name, pathElement);
        return pathElement;
    }

    public <E extends PathElement> E addChild(E child) {
        children.put(child.getName(), child);
        return child;
    }

    public ArrayPathElement addArrayChild() {
        var pathElement = new ArrayPathElement(this);
        children.put(pathElement.getName(), pathElement);
        return pathElement;
    }


    public PathElement getChild(String name) {
        return children.get(name);
    }

    @Override
    public String toString() {
        return "PathElement{" + getPath() + "}";
    }
}
