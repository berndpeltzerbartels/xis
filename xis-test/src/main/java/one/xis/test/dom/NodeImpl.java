package one.xis.test.dom;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Predicate;


@Getter
@Setter
@SuppressWarnings("unused")
public abstract class NodeImpl extends GraalVMProxy implements Node {


    private org.jsoup.nodes.Node jsoupNode;

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
        // 1) eigene Baumstruktur aktualisieren
        var previousSibling = getPreviousSibling();
        if (previousSibling != null) {
            previousSibling.setNextSibling(getNextSibling());
            if (previousSibling == previousSibling.getNextSibling()) {
                throw new IllegalStateException();
            }
        }
        if (parentNode != null && parentNode.getFirstChild() == this) {
            parentNode.setFirstChild(getNextSibling());
        }
        setNextSibling(null);
        if (parentNode != null) {
            parentNode.updateChildNodes();
        }

        // 2) jsoup-Back­ing (falls vorhanden) mitziehen
        if (jsoupNode != null) {
            jsoupNode.remove(); // jsoup entfernt sich selbst aus dem Parent
        }

        parentNode = null;
    }

    @Override
    public void appendChild(Node node) {
        var nodeImpl = (NodeImpl) node;
        if (nodeImpl.getParentNode() != null) {
            nodeImpl.remove();
        }
        // 1) eigene Baumstruktur verketten
        nodeImpl.parentNode = this;
        if (firstChild == null) {
            firstChild = nodeImpl;
        } else {
            var lastChild = firstChild;
            while (lastChild.getNextSibling() != null) {
                lastChild = lastChild.getNextSibling();
            }
            lastChild.setNextSibling(nodeImpl);
        }
        nodeImpl.setNextSibling(null);
        updateChildNodes();

        // 2) jsoup-Back­ing (falls vorhanden) synchronisieren
        if (this.jsoupNode instanceof org.jsoup.nodes.Element parentJsoup && nodeImpl.jsoupNode != null) {
            // jsoup: an Parent anhängen (Node wird ggf. automatisch aus altem Parent entfernt)
            parentJsoup.appendChild(nodeImpl.jsoupNode);
        }
    }

    @Override
    public void removeChild(Node b) {
        getChildNodes().stream().filter(n -> n == b).findFirst().ifPresent(Node::remove);
    }

    @Override
    public void insertBefore(@NonNull Node before, Node marker) {
        NodeImpl beforeImpl = (NodeImpl) before;
        NodeImpl markerImpl = (NodeImpl) marker;
        if (markerImpl == null) {
            appendChild(beforeImpl);
            updateChildNodes();
            return;
        }
        if (beforeImpl.getParentNode() != null) {
            beforeImpl.remove();
        }
        var previousChild = markerImpl.getPreviousSibling();
        if (previousChild == null) {
            setFirstChild(beforeImpl);
        } else {
            previousChild.setNextSibling(beforeImpl);
        }
        beforeImpl.setNextSibling(markerImpl);
        beforeImpl.setParentNode(this);
        updateChildNodes();
    }

    @Override
    public NodeList getElementsByTagName(String name) {
        var list = new ArrayList<Node>();
        findElements(e -> e.getLocalName().equals(name), list);
        return new NodeList(list);
    }

    void updateChildNodes() {
        childNodes.updateChildNodes(this);
    }

    void updateTreeByChildNodes() {
        firstChild = childNodes.length > 0 ? (NodeImpl) childNodes.item(0) : null;
        for (int i = 0; i < childNodes.length; i++) {
            NodeImpl child = (NodeImpl) childNodes.item(i);
            child.parentNode = this;
            child.setNextSibling(i + 1 < childNodes.length ? (NodeImpl) childNodes.item(i + 1) : null);
        }
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

    protected void findElements(Predicate<ElementImpl> predicate, Collection<Node> result) {
        var sibling = getNextSibling();
        while (sibling != null) {
            if (sibling instanceof ElementImpl element) {
                element.findElements(predicate, result);
                break;
            }
            sibling = sibling.getNextSibling();
        }
        if (getFirstChild() != null) {
            getFirstChild().findElements(predicate, result);
        }
    }

    /**
     * Unterklassen rendern ihre HTML-Repräsentation (Fallback, wenn kein jsoup vorhanden ist).
     */
    protected abstract void evaluateContent(StringBuilder builder, int indent);

    @Override
    public Node getParentElement() {
        NodeImpl p = getParentNode();
        while (p != null && !(p instanceof ElementImpl)) {
            p = p.getParentNode();
        }
        return p;
    }

}
