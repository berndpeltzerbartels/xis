package one.xis.html.document;

public interface Node {
    String toHtml();

    Node getNextSibling();

    void setParentNode(Element parent);

    Element getParentNode();

    void setNextSibling(Node nextSibling);
}
