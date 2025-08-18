package one.xis.test.dom;

public interface Node {

    int ELEMENT_NODE = 1;
    int TEXT_NODE = 3;

    void remove();

    Node cloneNode();

    void appendChild(Node node);

    // Non standard methods for testing purposes

    Node getParentNode();

    Node getFirstChild();

    Node getNextSibling();

    NodeList getChildNodes();

    Element getParentElement();

    String asString();
}
