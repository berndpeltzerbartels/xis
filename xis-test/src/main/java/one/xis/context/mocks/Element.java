package one.xis.context.mocks;

import lombok.Getter;
import lombok.NonNull;

import java.util.*;

@Getter
public class Element extends Node {

    private final String tagName;
    private final Document document;

    @Getter
    private final List<Node> children = new ArrayList<>();
    private final Map<String, String> attributes = new HashMap<>();

    public Element(String tagName, Document document) {
        this.tagName = tagName;
        this.document = document;
        this.document.getElementsByTagName().computeIfAbsent(tagName, name -> new HashSet<>()).add(this);
    }

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
