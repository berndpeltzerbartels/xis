package one.xis.test.mocks;

import lombok.Getter;
import lombok.NonNull;

import java.util.*;


public class Document {

    private final Map<String, Collection<Element>> elementsByTagName = new HashMap<>();
    private final Map<String, Collection<Element>> elementsByClass = new HashMap<>();
    private final Map<String, Element> elementsById = new HashMap<>();

    @Getter
    private final Element rootElement;

    public Document() {
        // These tags are never created but part of the document
        this.rootElement = new Element("html", this);
        var head = new Element("head", this);
        var body = new Element("body", this);
        var title = new Element("title", this);
        this.rootElement.appendChild(head);
        this.rootElement.appendChild(body);
        head.appendChild(title);
    }

    public Element getElementById(String id) {
        return elementsById.get(id);
    }

    public NodeList getElementsByTagName(String name) {
        return new NodeList(Optional.ofNullable(elementsByTagName.get(name))
                .orElse(Collections.emptyList()));
    }

    public Collection<Element> getElementsByClass(String styleClass) {
        return Optional.ofNullable(elementsByClass.get(styleClass)).orElse(Collections.emptyList());
    }

    void registerELement(Element element) {
        var id = element.getAttribute("id");
        if (id != null) {
            elementsById.put(id, element);
        }
        var styleClass = element.getAttribute("class");
        if (styleClass != null) {
            elementsByClass.computeIfAbsent(styleClass, cl -> new HashSet<>()).add(element);
        }
        elementsByTagName.computeIfAbsent(element.getLocalName(), name -> new HashSet<>()).add(element);
    }


    public Element createElement(@NonNull String tagname) {
        return new Element(tagname, this);
    }

    public TextNode createTextNode(String text) {
        return new TextNode(text);
    }


}
