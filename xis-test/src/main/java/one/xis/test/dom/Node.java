package one.xis.test.dom;

public interface Node {
    String getTextContent();

    void insertPreviousSibling(Node node);

    void remove();

    void setNextSibling(Node node);

    Node getNextSibling();

    Node cloneNode();

    String getName();

    Node getLastSibling();

    Node getPreviousSibling();

    Element getParentNode();

    String asString();
}
