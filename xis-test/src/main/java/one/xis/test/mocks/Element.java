package one.xis.test.mocks;

import lombok.Getter;
import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;

@Getter
public class Element extends Node {

    public String localName;
    private final Document document;
    public final NodeList childNodes = new NodeList();
    public String innerText;
    public String innerHtml;
    private final Map<String, String> attributes = new HashMap<>();

    public Element(String tagName, Document document) {
        this.localName = tagName;
        this.document = document;
    }

    public void setAttribute(@NonNull String name, @NonNull String value) {
        attributes.put(name, value);
        updateDocument();
    }

    public String[] getAttributeNames() {
        return attributes.keySet().toArray(String[]::new);
    }

    public String getAttribute(@NonNull String name) {
        return attributes.get(name);
    }

    public void appendChild(@NonNull Node node) {
        childNodes.addItem(node);
        if (node instanceof Element) {
            ((Element) node).updateDocument();
        }
    }

    public void removeChild(@NonNull Node node) {
        childNodes.remove(node);
    }

    private void updateDocument() {
        document.registerELement(this);
    }

    @Override
    public String toString() {
        return "Element{" +
                "localName='" + localName + '\'' +
                '}';
    }
}
