package one.xis.test.dom;

import lombok.Getter;
import org.graalvm.polyglot.proxy.ProxyObject;

import java.util.Set;

@Getter
public abstract class NodeImpl implements Node, ProxyObject {
    public ElementImpl parentNode;
    public Node nextSibling;

    public static final int ELEMENT_NODE = 4;

    static final Set<String> DECLARED_FIELDS = Set.of(
            "parentNode",
            "nextSibling"
    );

    Object getFieldValue(String fieldName) {
        return switch (fieldName) {
            case "parentNode" -> parentNode;
            case "nextSibling" -> nextSibling;
            default -> throw new IllegalArgumentException("Unknown field: " + fieldName);
        };
    }

    @Override
    public void insertPreviousSibling(Node node) {
        var nodeImpl = (NodeImpl) node;
        nodeImpl.parentNode = this.parentNode;
        var previousSibling = (NodeImpl) getPreviousSibling();
        if (previousSibling != null) {
            previousSibling.setNextSibling(node);
            if (previousSibling == previousSibling.getNextSibling()) {
                throw new IllegalStateException();
            }
        }
        node.setNextSibling(this);
        if (node == nodeImpl.getNextSibling()) {
            throw new IllegalStateException();
        }
        if (nodeImpl.parentNode.firstChild == this) {
            nodeImpl.parentNode.firstChild = node;
        }
    }

    @Override
    public void remove() {
        var previousSibling = (NodeImpl) getPreviousSibling();
        if (previousSibling != null) {
            previousSibling.setNextSibling(getNextSibling());
            if (previousSibling == previousSibling.getNextSibling()) {
                throw new IllegalStateException();
            }
        }

        if (parentNode.firstChild == this) {
            parentNode.firstChild = getNextSibling();
        }
        setNextSibling(null);
        parentNode.updateChildNodes();
        parentNode = null;
    }


    @Override
    public void setNextSibling(Node node) {
        if (this.equals(node)) {
            return;
            // does not work in integration-etst. behaviour of DOM in browser is differen
            // throw new IllegalStateException("Node cannot be its own sibling: " + this);
        }
        nextSibling = node;
    }

    @Override
    public Node getLastSibling() {
        if (getNextSibling() == null) {
            return this;
        }
        return getNextSibling().getLastSibling();
    }

    @Override
    public Node getPreviousSibling() {
        if (parentNode == null) {
            return null;
        }
        var prev = parentNode.firstChild;
        if (prev == null) {
            return null;
        }
        var node = prev.getNextSibling();
        while (node != null && prev != null) {
            if (node.equals(this)) {
                return prev;
            }
            node = node.getNextSibling();
            prev = ((NodeImpl) prev).getNextSibling();
        }
        return null;
    }

    protected abstract void evaluateContent(StringBuilder builder, int indent);
}
