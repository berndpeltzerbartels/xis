package one.xis.test.dom;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@SuppressWarnings("unused")
public abstract class NodeImpl extends GraalVMProxy implements Node {

    /**
     * Optional: zugehöriger jsoup-Knoten (falls vorhanden)
     */
    private org.jsoup.nodes.Node jsoupNode;

    private NodeImpl parentNode;
    private NodeImpl nextSibling;
    private NodeImpl firstChild;
    private final NodeList childNodes = new NodeList();
    private final int nodeType;

    /**
     * Standard-Konstruktor (ohne jsoup-Back­ing)
     */
    public NodeImpl(int nodeType) {
        this.nodeType = nodeType;
    }

    /**
     * Optionaler Konstruktor mit jsoup-Back­ing-Knoten
     */
    public NodeImpl(int nodeType, org.jsoup.nodes.Node jsoupNode) {
        this.nodeType = nodeType;
        this.jsoupNode = jsoupNode;
    }

    /**
     * Kann von Unterklassen (z. B. ElementImpl/TextNodeImpl) benutzt werden,
     * um nachträglich einen jsoup-Knoten zu hinterlegen.
     */
    protected void attachJsoupNode(org.jsoup.nodes.Node jsoupNode) {
        this.jsoupNode = jsoupNode;
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
        if (parentNode != null && parentNode.firstChild == this) {
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

    void updateChildNodes() {
        childNodes.updateChildNodes(this);
        // Hinweis: die jsoup-Kindliste wird durch append/remove bereits aktuell gehalten.
        // Wenn du eine explizite Resync-Strategie brauchst (z. B. aus jsoup -> eigener Baum),
        // könntest du sie hier optional implementieren.
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

    /**
     * Unterklassen rendern ihre HTML-Repräsentation (Fallback, wenn kein jsoup vorhanden ist).
     */
    protected abstract void evaluateContent(StringBuilder builder, int indent);

    @Override
    public Element getParentElement() {
        return (Element) parentNode;
    }
}
