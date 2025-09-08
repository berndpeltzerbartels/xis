package one.xis.html.document;

public interface Node {
    String asString();

    Node getNextSibling();

    void setParentNode(Element parent);

    Element getParentNode();
}
