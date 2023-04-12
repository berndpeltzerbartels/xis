package one.xis.test.dom;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Document {

    public Element rootNode;

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


}
