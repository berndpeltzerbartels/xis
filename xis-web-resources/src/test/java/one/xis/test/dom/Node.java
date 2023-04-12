package one.xis.test.dom;

public abstract class Node {
    public Node nextSibling;
    public Element parentNode;
    Node previousSibling;

    void insertPreviousSibling(Node node) {
        node.parentNode = this.parentNode;
        if (previousSibling != null) {
            previousSibling.nextSibling = node;
        }
        node.previousSibling = previousSibling;
        node.nextSibling = this;
        previousSibling = node;
    }

    void remove() {
        if (parentNode.firstChild == this) {
            parentNode.firstChild = null;
        }
        if (previousSibling != null) {
            previousSibling.nextSibling = nextSibling;
        }
        if (nextSibling != null) {
            nextSibling.previousSibling = previousSibling;
        }
        parentNode.updateChildNodes();
        parentNode = null;
    }

    abstract Node cloneNode();

}
