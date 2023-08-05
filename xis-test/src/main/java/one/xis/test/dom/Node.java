package one.xis.test.dom;

import java.util.function.Consumer;

public abstract class Node {
    public Node nextSibling;
    public Element parentNode;
    public Consumer<Object> _refresh; // We add this method during initialization

    void insertPreviousSibling(Node node) {
        node.parentNode = this.parentNode;
        var previousSibling = getPreviousSibling();
        if (previousSibling != null) {
            previousSibling.setNextSibling(node);
            if (previousSibling == previousSibling.nextSibling) {
                throw new IllegalStateException();
            }
        }
        node.setNextSibling(this);
        if (node == node.nextSibling) {
            throw new IllegalStateException();
        }
        if (node.parentNode.firstChild == this) {
            node.parentNode.firstChild = node;
        }
    }

    void remove() {
        var previousSibling = getPreviousSibling();
        if (previousSibling != null) {
            previousSibling.setNextSibling(nextSibling);
            if (previousSibling == previousSibling.nextSibling) {
                throw new IllegalStateException();
            }
        }

        if (parentNode.firstChild == this) {
            parentNode.firstChild = nextSibling;
        }
        setNextSibling(null);
        parentNode.updateChildNodes();
        parentNode = null;
    }


    void setNextSibling(Node node) {
        if (this.equals(node)) {
            throw new IllegalStateException();
        }
        nextSibling = node;
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
        if (parentNode == null) {
            return null;
        }
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

    protected abstract void evaluateContent(StringBuilder builder);
}
