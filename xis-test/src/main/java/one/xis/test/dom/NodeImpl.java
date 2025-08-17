package one.xis.test.dom;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@SuppressWarnings("unused")
public abstract class NodeImpl extends GraalVMProxy implements Node {
    private NodeImpl parentNode;
    private NodeImpl nextSibling;
    private NodeImpl firstChild;
    private final NodeList childNodes = new NodeList();
    private final int nodeType;

    public NodeImpl(int nodeType) {
        this.nodeType = nodeType;
    }

    @Override
    public void remove() {
        var previousSibling = getPreviousSibling();
        if (previousSibling != null) {
            previousSibling.setNextSibling(getNextSibling());
            if (previousSibling == previousSibling.getNextSibling()) {
                throw new IllegalStateException();
            }
        }

        if (parentNode.firstChild == this) {
            parentNode.setFirstChild(getNextSibling());
        }
        setNextSibling(null);
        parentNode.updateChildNodes();
        parentNode = null;
    }


    @Override
    public void appendChild(Node node) {
        var proxy = (NodeImpl) node;
        proxy.parentNode = this;
        if (firstChild == null) {
            firstChild = proxy;
        } else {
            var lastChild = firstChild;
            while (lastChild.getNextSibling() != null) {
                lastChild = lastChild.getNextSibling();
            }
            lastChild.setNextSibling(proxy);
        }
        proxy.setNextSibling(null);
        updateChildNodes();
    }

    void updateChildNodes() {
        childNodes.updateChildNodes(this);
    }

    NodeImpl getPreviousSibling() {
        if (getParentNode() == null) {
            return null;
        }
        if (getParentNode().getFirstChild() == null) {
            throw new IllegalStateException("Parent node has no children");
        }
        var previous = parentNode.firstChild;
        while (previous != null && previous.getNextSibling() != this) {
            previous = previous.getNextSibling();
        }
        return previous;
    }


    protected abstract void evaluateContent(StringBuilder builder, int i);
}