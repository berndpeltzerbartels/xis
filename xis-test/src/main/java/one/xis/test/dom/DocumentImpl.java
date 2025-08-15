package one.xis.test.dom;

import lombok.Getter;
import one.xis.utils.io.IOUtils;

import java.util.*;
import java.util.function.Predicate;

@Getter
public class DocumentImpl implements Document {

    public ElementImpl documentElement;

    public Location location = new Location();

    public String cookies = "";

    public DocumentImpl(String rootTagName) {
        this(new ElementImpl(rootTagName));
    }

    public DocumentImpl(ElementImpl documentElement) {
        this.documentElement = documentElement;
    }

    public Element createElement(String name) {
        return switch (name) {
            case "input" -> new InputElementImpl();
            case "select" -> new SelectElement();
            case "option" -> new OptionElementImpl();
            case "textarea" -> new TextareaElement();
            default -> new ElementImpl(name);
        };
    }

    @Override
    public Element querySelector(String selector) {
        return documentElement.querySelector(selector);
    }

    @Override
    public List<Element> querySelectorAll(String selector) {
        return documentElement.querySelectorAll(selector);
    }

    public TextNode createTextNode(String content) {
        return new TextNodeIml(content);
    }

    public void setTextContent(String text) {
        if (documentElement != null) {
            documentElement.setInnerText(text);
        }
    }

    @Override
    public String getTextContent() {
        return documentElement != null ? documentElement.getInnerText() : null;
    }

    @Override
    public Element getBody() {
        return documentElement.getChildElementByName("body");
    }

    @Override
    public Element getHead() {
        return documentElement.getChildElementByName("head");
    }

    @Override
    public String getTitle() {
        return Optional.ofNullable(getHead())
                .map(head -> head.getChildElementByName("title"))
                .map(Element::getInnerText)
                .orElse(null);
    }

    @Override
    public NodeList getElementsByTagName(String name) {
        var nodeList = new NodeList();
        documentElement.findByTagName(name, nodeList);
        return nodeList;
    }

    @Override
    public Element getElementById(String id) {
        return documentElement.getDescendantById(id);
    }

    @Override
    public InputElement getInputElementById(String id) {
        return (InputElement) documentElement.getDescendantById(id);
    }

    @Override
    public List<Element> getElementsByClass(String cssClass) {
        var list = new ArrayList<Element>();
        documentElement.findByClass(cssClass, list);
        return list;
    }

    @Override
    public Element getElementByTagName(String name) {
        var list = getElementsByTagName(name);
        switch (list.length) {
            case 0:
                return null;
            case 1:
                return (ElementImpl) list.item(0);
            default:
                throw new IllegalStateException("too many results for " + name);
        }
    }

    public static Document fromResource(String classPathResource) {
        return of(IOUtils.getResourceAsString(classPathResource));
    }

    @Override
    public Element findElement(Predicate<Element> predicate) {
        var result = new ArrayList<>(findElements(predicate));
        return switch (result.size()) {
            case 0 -> throw new NoSuchElementException();
            case 1 -> result.get(0);
            default -> throw new IllegalStateException("too many results");
        };
    }

    @Override
    public String asString() {
        return documentElement != null ? documentElement.asString() : null;
    }

    // TODO test
    @Override
    public Collection<Element> findElements(Predicate<Element> predicate) {
        var results = new HashSet<Element>();
        if (this.getDocumentElement() != null) {
            documentElement.findElements(predicate, results);
        }
        return results;
    }

    public static Document of(String html) {
        return DocumentBuilder.build(html);
    }

}
