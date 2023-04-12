package one.xis.test.dom;


import lombok.Getter;

@Getter
public class Element extends Node {

    public final String localName;
    public Node firstChild;
    public final NodeList childNodes = new NodeList();

    public Element(String tagName) {
        this.localName = tagName;
    }

    public void appendChild(Node node) {
        if (firstChild == null) {
            firstChild = node;
        } else {
            firstChild.nextSibling = node;
            node.previousSibling = firstChild;
        }
        node.parentNode = this;
        updateChildNodes();
    }

    public void insertBefore(Node node, Node next) {
        next.insertPreviousSibling(node);
        updateChildNodes();
    }

    public void removeChild(Node node) {
        if (node.parentNode != this) {
            throw new IllegalStateException("not a child");
        }
        node.remove();
    }

    void findByTagName(String name, NodeList result) {
        if (localName.equals(name)) {
            result.addNode(this);
        }
        if (nextSibling != null && nextSibling instanceof Element) {
            ((Element) nextSibling).findByTagName(name, result);
        }
        if (firstChild != null && firstChild instanceof Element) {
            ((Element) firstChild).findByTagName(name, result);
        }
    }

    @Override
    public String toString() {
        return "<" + localName + ">";
    }

    void updateChildNodes() {
        childNodes.clear();
        var child = firstChild;
        while (child != null) {
            childNodes.addNode(child);
            child = child.nextSibling;
        }
    }

}
