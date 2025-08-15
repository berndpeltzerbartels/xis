package one.xis.test.dom;

import lombok.Getter;
import lombok.NonNull;
import one.xis.utils.lang.StringUtils;
import org.graalvm.polyglot.proxy.ProxyObject;

import java.util.Objects;
import java.util.stream.Collectors;

@Getter
public abstract class NodeImpl implements Node, ProxyObject {
    public ElementImpl parentNode;
    public Node nextSibling;
    public Node firstChild;
    public final NodeList childNodes = new NodeList();
    public final int nodeType;

    public static final int ELEMENT_NODE = 1;
    public static final int TEXT_NODE = 3;

    protected NodeImpl(int nodeType) {
        this.nodeType = nodeType;
    }


    @Override
    public String getTextContent() {
        return childNodes.stream()
                .filter(TextNodeIml.class::isInstance)
                .map(TextNodeIml.class::cast)
                .map(TextNode::getNodeValue)
                .map(StringUtils::toString)
                .filter(Objects::nonNull)
                .collect(Collectors.joining()).trim();
    }

    @SuppressWarnings("unused") // Used by proxy
    public void setTextContent(String textContent) {
        this.childNodes.clear();
        this.setFirstChild(null);
        this.appendChild(new TextNodeIml(textContent));
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
            nodeImpl.parentNode.setFirstChild(node);
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
            parentNode.setFirstChild(getNextSibling());
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

    public void setFirstChild(Node node) {
        if (this.equals(node)) {
            throw new IllegalStateException();
        }
        firstChild = node;
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
        if (prev.equals(this)) {
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

    public void appendChild(@NonNull Node node) {
        node.setNextSibling(null);
        if (firstChild == null) {
            setFirstChild(node);
        } else {
            var last = (NodeImpl) firstChild.getLastSibling();
            last.setNextSibling(node);
            if (last == last.getNextSibling()) {
                throw new IllegalStateException();
            }
        }

        ((NodeImpl) node).parentNode = (ElementImpl) this;
        updateChildNodes();

    }


    void updateChildNodes() {
        childNodes.clear();
        var child = firstChild;
        while (child != null) {
            childNodes.addNode(child);
            child = child.getNextSibling();
        }
    }
}