package one.xis.context.mocks;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.*;

@RequiredArgsConstructor
public class Element extends Node {

    private final String tagName;
    private final Document document;

    @Getter
    private final List<Node> children = new ArrayList<>();
    private final Map<String, String> attributes = new HashMap<>();

    public void setAttribute(@NonNull String name, @NonNull String value) {
        if (name.equals("id")) {
            document.getElementsById().put(name, this);
        } else if (name.equals("class")) {
            Arrays.stream(value.split(" +")).forEach(cl -> document.getElementsByClass().put(cl, this));
        }
        attributes.put(name, value);
    }

    public String[] getAttributeNames() {
        return attributes.keySet().toArray(String[]::new);
    }

    public String getAttribute(@NonNull String name) {
        return attributes.get(name);
    }

    public void appendChild(@NonNull Node node) {
        children.add(node);
    }

    public void removeChild(@NonNull Node node) {
        children.remove(node);
    }
}
