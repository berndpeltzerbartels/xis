package one.xis.test.dom;

import one.xis.utils.lang.StringUtils;
import one.xis.utils.xml.XmlUtil;
import org.w3c.dom.Node;

class DocumentBuilder {

    static Document build(String html) {
        var w3cDoc = XmlUtil.loadDocument(html);
        var rootName = w3cDoc.getDocumentElement().getTagName();
        var document = new DocumentImpl(rootName);
        copyAttributes(w3cDoc.getDocumentElement(), document.getDocumentElement());
        evaluate(w3cDoc.getDocumentElement(), document.getDocumentElement());
        return document;
    }

    private static void evaluate(org.w3c.dom.Element src, ElementImpl dest) {
        var nodeList = src.getChildNodes();
        for (var i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node instanceof org.w3c.dom.Element w3cElement) {
                var e = translateElement(w3cElement);
                dest.appendChild(e);
                copyAttributes(w3cElement, e);
                evaluate(w3cElement, e);
            } else if (StringUtils.isNotEmpty(node.getNodeValue())) {
                dest.appendChild(new TextNodeIml(node.getNodeValue()));
            }
        }
    }

    private static ElementImpl translateElement(org.w3c.dom.Element w3cElement) {
        return Element.createElement(w3cElement.getTagName());
    }

    private static void copyAttributes(org.w3c.dom.Element src, ElementImpl dest) {
        for (int i = 0; i < src.getAttributes().getLength(); i++) {
            var attribute = src.getAttributes().item(i);
            dest.setAttribute(attribute.getNodeName(), attribute.getNodeValue());
        }
    }
}
