package one.xis.test.dom;

import lombok.Getter;

public class TextNode extends Node {

    @Getter
    public String nodeValue;

    public static final int nodeType = 3;
    @SuppressWarnings("unused")
    public Object _expression;

    public TextNode(String nodeValue) {
        this.nodeValue = nodeValue;
    }

    @Override
    public Node cloneNode() {
        return new TextNode(nodeValue);
    }

    @Override
    public String getName() {
        return "TextNode";
    }

    @Override
    public String toString() {
        return "TextNode(" + nodeValue + ")";
    }
}
