package one.xis.test.dom;

public abstract class Node {
    public Node nextSibling;
    public Element parentNode;

    void insertPreviousSibling(Node node) {
        node.parentNode = this.parentNode;
        var previousSibling = getPreviousSibling();
        if (previousSibling != null) {
            previousSibling.nextSibling = node;
        }
        node.nextSibling = this;
    }

    void remove() {
        var previousSibling = getPreviousSibling();
        if (previousSibling != null) {
            previousSibling.nextSibling = nextSibling;
        }
        if (parentNode.firstChild == this) {
            parentNode.firstChild = null;
        }
        parentNode.updateChildNodes();
        parentNode = null;
    }

    public abstract Node cloneNode();

    public abstract String getName();

    public Node getLastSibling() {
        if (nextSibling == null) {
            return this;
        }
        return nextSibling.getLastSibling();
    }

    Node getPreviousSibling() {
        var prev = parentNode.firstChild;
        if (prev == null) {
            return null;
        }
        var node = prev.nextSibling;
        while (node != null && prev != null) {
            if (node.equals(this)) {
                return prev;
            }
            node = node.nextSibling;
            prev = prev.nextSibling;
        }
        return null;
    }

}
