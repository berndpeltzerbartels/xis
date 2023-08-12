package one.xis.test.dom;

import one.xis.utils.io.IOUtils;
import one.xis.utils.lang.StringUtils;
import one.xis.utils.xml.XmlUtil;

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

    public Document(String rootTagName) {
        this(new Element(rootTagName));
    }

    public Document(Element rootNode) {
        this.rootNode = rootNode;
    }

    public Element createElement(String name) {
        return new Element(name);
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
        var w3cDoc = XmlUtil.loadDocument(html);
        var rootName = w3cDoc.getDocumentElement().getTagName();
        var document = new Document(rootName);
        copyAttributes(w3cDoc.getDocumentElement(), document.rootNode);
        evaluate(w3cDoc.getDocumentElement(), document.rootNode);
        return document;
    }

    public static void evaluate(org.w3c.dom.Element src, Element dest) {
        var nodeList = src.getChildNodes();
        for (var i = 0; i < nodeList.getLength(); i++) {
            org.w3c.dom.Node node = nodeList.item(i);
            if (node instanceof org.w3c.dom.Element) {
                var w3cElement = (org.w3c.dom.Element) node;
                var e = new Element(w3cElement.getTagName());
                dest.appendChild(e);
                copyAttributes(w3cElement, e);
                evaluate(w3cElement, e);
            } else if (StringUtils.isNotEmpty(node.getNodeValue())) {
                dest.appendChild(new TextNode(node.getNodeValue()));
                dest.innerText = node.getNodeValue();
            }
        }
    }

    private static void copyAttributes(org.w3c.dom.Element src, Element dest) {
        for (int i = 0; i < src.getAttributes().getLength(); i++) {
            var attribute = src.getAttributes().item(i);
            dest.setAttribute(attribute.getNodeName(), attribute.getNodeValue());
        }
    }
}
