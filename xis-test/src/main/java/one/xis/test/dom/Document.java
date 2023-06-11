package one.xis.test.dom;

import one.xis.utils.io.IOUtils;
import one.xis.utils.lang.StringUtils;
import one.xis.utils.xml.XmlUtil;

import java.util.ArrayList;
import java.util.List;

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
        return rootNode.getElementById(id);
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
