package one.xis.validation;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
public class ArrayPathElement implements PathElement {
    private final String name;
    private final String path;
    private final List<PathElement> children = new ArrayList<>();

    public ArrayPathElement(DefaultPathElement parent) {
        this(parent.getName(), parent.getPath());
        if (parent instanceof RootPathElement) {
            throw new IllegalArgumentException();
        }
    }

    public ArrayPathElement(RootPathElement parent) {
        this(parent.getName(), "/");
    }

    public DefaultPathElement addChild() {
        var index = children.size();
        var pathElement = new DefaultPathElement(name, path + String.format("[%d]", index));
        children.add(pathElement);
        return pathElement;
    }

}
