package one.xis.test.dom;

import one.xis.utils.io.IOUtils;

import java.util.*;
import java.util.function.Predicate;

/**
 * Document used as mock for html-documents. Sorrily {@link org.w3c.dom.Element} is using
 * getters instead of fields. So it can not be used for html-testing.
 */
@SuppressWarnings("unused")
public class Document {

    public Element rootNode;

    public Location location = new Location();

    public String cookie = "";

    public Document(String rootTagName) {
        this(new Element(rootTagName));
    }

    public Document(Element rootNode) {
        this.rootNode = rootNode;
    }

    public Element createElement(String name) {
        return switch (name) {
            case "input" -> new InputElement();
            case "select" -> new SelectElement();
            case "option" -> new OptionElement();
            case "textarea" -> new TextareaElement();
            default -> new Element(name);
        };
    }


    public TextNode createTextNode(String content) {
        return new TextNode(content);
    }

    public NodeList getElementsByTagName(String name) {
        var nodeList = new NodeList();
        rootNode.findByTagName(name, nodeList);
        return nodeList;
    }

    public Element getElementById(String id) {
        return rootNode.getDescendantById(id);
    }

    public InputElement getInputElementById(String id) {
        return (InputElement) rootNode.getDescendantById(id);
    }

    public List<Element> getElementsByClass(String cssClass) {
        var list = new ArrayList<Element>();
        rootNode.findByClass(cssClass, list);
        return list;
    }

    public Element getElementByTagName(String name) {
        var list = getElementsByTagName(name);
        switch (list.length) {
            case 0:
                return null;
            case 1:
                return (Element) list.item(0);
            default:
                throw new IllegalStateException("too many results for " + name);
        }
    }

    public static Document fromResource(String classPathResource) {
        return of(IOUtils.getResourceAsString(classPathResource));
    }

    public Element findElement(Predicate<Element> predicate) {
        var result = new ArrayList<>(findElements(predicate));
        return switch (result.size()) {
            case 0 -> throw new NoSuchElementException();
            case 1 -> result.get(0);
            default -> throw new IllegalStateException("too many results");
        };
    }

    public String asString() {
        return rootNode != null ? rootNode.asString() : null;
    }

    // TODO test
    public Collection<Element> findElements(Predicate<Element> predicate) {
        var results = new HashSet<Element>();
        if (this.rootNode != null) {
            rootNode.findElements(predicate, results);
        }
        return results;
    }

    public void replaceRoot(Element element) {
        this.rootNode = element;
    }

    public static Document of(String html) {
        return DocumentBuilder.build(html);
    }

    public void addCookie(String name, String value) {
        if (cookie.isEmpty()) {
            cookie = name + "=" + value;
        } else {
            cookie += "; " + name + "=" + value;
        }
    }


    private InputElement createInputElement(String type) {
        return new InputElement();
    }
}
