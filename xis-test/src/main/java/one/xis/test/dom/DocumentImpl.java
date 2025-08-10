package one.xis.test.dom;

import lombok.Getter;

import java.util.List;
import java.util.Optional;

@Getter
public class DocumentImpl implements Document {

    public final ElementImpl documentElement;

    public Location location = new Location();

    public String cookies = "";

    public DocumentImpl(String rootTagName) {
        this(new ElementImpl(rootTagName));
    }

    public DocumentImpl(ElementImpl documentElement) {
        this.documentElement = documentElement;
    }

    public Element createElement(String name) {
        return Element.createElement(name);
    }

    @Override
    public Element querySelector(String selector) {
        return documentElement.querySelector(selector);
    }

    @Override
    public List<Element> querySelectorAll(String selector) {
        return documentElement.querySelectorAll(selector);
    }

    @Override
    public TextNode createTextNode(String content) {
        return new TextNodeIml(content);
    }

    public String getInnerText() {
        return documentElement != null ? documentElement.getInnerText() : null;
    }

    Element getBody() {
        return documentElement.getElementByTagName("body");
    }

    Element getHead() {
        return documentElement.getElementByTagName("head");
    }

    String getTitle() {
        return Optional.ofNullable((ElementImpl) getHead())
                .map(head -> (ElementImpl) head.getElementByTagName("title"))
                .map(ElementImpl::getInnerText)
                .orElse(null);
    }

    @Override
    public NodeList getElementsByTagName(String name) {
        return documentElement.getElementsByTagName(name);
    }

    @Override
    public Element getElementById(String id) {
        return documentElement.getElementById(id);
    }

    @Override
    public InputElement getInputElementById(String id) {
        var e = getElementById(id);
        return e instanceof InputElement inputElement ? inputElement : null;
    }


    @Override
    public Element getElementByTagName(String name) {
        var list = getElementsByTagName(name);
        return switch (list.length) {
            case 0 -> null;
            case 1 -> (ElementImpl) list.item(0);
            default -> throw new IllegalStateException("too many results for " + name);
        };
    }

    @Override
    public String asString() {
        return documentElement != null ? documentElement.asString() : null;
    }

    @Override
    public List<Element> getElementsByClass(String item) {
        return documentElement.getElementsByClass(item);
    }

}
