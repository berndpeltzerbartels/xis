package one.xis.test.dom;

import one.xis.utils.lang.StringUtils;
import one.xis.utils.xml.XmlUtil;

class DocumentBuilder {

    static Document build(String html) {
        var w3cDoc = XmlUtil.loadDocument(html);
        var rootName = w3cDoc.getDocumentElement().getTagName();
        var document = new Document(rootName);
        copyAttributes(w3cDoc.getDocumentElement(), document.rootNode);
        evaluate(w3cDoc.getDocumentElement(), document.rootNode);
        return document;
    }

    private static void evaluate(org.w3c.dom.Element src, Element dest) {
        var nodeList = src.getChildNodes();
        for (var i = 0; i < nodeList.getLength(); i++) {
            org.w3c.dom.Node node = nodeList.item(i);
            if (node instanceof org.w3c.dom.Element w3cElement) {
                var e = translateElement(w3cElement);
                dest.appendChild(e);
                copyAttributes(w3cElement, e);
                evaluate(w3cElement, e);
            } else if (StringUtils.isNotEmpty(node.getNodeValue())) {
                dest.appendChild(new TextNode(node.getNodeValue()));
                dest.innerText = node.getNodeValue();
            }
        }
    }

    private static Element translateElement(org.w3c.dom.Element w3cElement) {
        return switch (w3cElement.getTagName()) {
            case "input" -> new InputElement();
            case "select" -> new SelectElement();
            case "option" -> new OptionElement();
            case "textarea" -> new TextareaElement();
            default -> new Element(w3cElement.getTagName());
        };
    }

    private static void copyAttributes(org.w3c.dom.Element src, Element dest) {
        for (int i = 0; i < src.getAttributes().getLength(); i++) {
            var attribute = src.getAttributes().item(i);
            dest.setAttribute(attribute.getNodeName(), attribute.getNodeValue());
        }
    }
}
