package one.xis.test.dom;

import lombok.Getter;


public class TextNodeImpl extends CharacterNode implements TextNode {

    @SuppressWarnings("unused") // used in js
    public static final int nodeType = 3;
    public Object _expression;

    public TextNodeImpl(String nodeValue) {
        super(TEXT_NODE, nodeValue);
    }

    @Override
    public Node cloneNode() {
        return new TextNodeImpl(getNodeValue());
    }

    @Override
    public String toString() {
        var value = getNodeValue() == null ? "" : getNodeValue();
        return new String("TextNode(" + value + ")");
    }
}
