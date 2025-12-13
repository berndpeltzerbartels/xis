package one.xis.test.dom;

public interface Node {

    int ELEMENT_NODE = 1;
    int TEXT_NODE = 3;
    int DOCUMENT_FRAGMENT_NODE = 11;

    void remove();

    Node cloneNode();

    void appendChild(Node node);

    // Non standard methods for testing purposes

    Node getParentNode();

    Node getFirstChild();

    Node getNextSibling();

    NodeList getChildNodes();

    Node getParentElement();

    String asString();

    default String asHTML() {
        return asString();
    }

    void removeChild(Node b);

    void insertBefore(Node b, Node a);

    NodeList getElementsByTagName(String name);
}
