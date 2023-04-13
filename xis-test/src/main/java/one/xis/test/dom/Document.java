package one.xis.test.dom;

import lombok.AllArgsConstructor;
import one.xis.utils.io.IOUtils;
import one.xis.utils.xml.XmlUtil;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@SuppressWarnings("unused")
public class Document {

    public final Element rootNode;

    public Document(String rootTagName) {
        this(new Element(rootTagName));
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
        return getElementById(id);
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

    public static Document of(String html) {
        var w3cDoc = XmlUtil.loadDocument(html);
        var rootName = w3cDoc.getDocumentElement().getTagName();
        var document = new Document(rootName);
        evaluate(w3cDoc.getDocumentElement(), document.rootNode);
        return document;
    }

    public static void evaluate(org.w3c.dom.Element src, Element dest) {
        var nodeList = src.getChildNodes();
        for (var i = 0; i < nodeList.getLength(); i++) {
            org.w3c.dom.Node node = nodeList.item(i);
            if (node instanceof Element) {
                var e = new Element(node.getLocalName());
                dest.appendChild(e);
                evaluate((org.w3c.dom.Element) node, e);
            } else {
                dest.appendChild(new TextNode(node.getNodeValue()));
            }
        }
    }
}
