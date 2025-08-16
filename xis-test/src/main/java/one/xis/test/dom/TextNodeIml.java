package one.xis.test.dom;

import lombok.Getter;


public class TextNodeIml extends NodeImpl implements TextNode {

    @Getter
    public String nodeValue;

    @SuppressWarnings("unused") // used in js
    public static final int nodeType = 3;
    public Object _expression;

    public TextNodeIml(String nodeValue) {
        super(TEXT_NODE);
        this.nodeValue = nodeValue;
    }

    @Override
    public Node cloneNode() {
        return new TextNodeIml(nodeValue);
    }

    public String asString() {
        return nodeValue != null ? nodeValue.toString() : "";
    }

    @Override
    public String toString() {
        var value = nodeValue == null ? "" : nodeValue;
        return new String("TextNode(" + value + ")");
    }


    @SuppressWarnings("unused")
    public void setNodeValue(String nodeValue) {
        this.nodeValue = nodeValue;
    }

    @Override
    protected void evaluateContent(StringBuilder builder, int i) {
        builder.append(nodeValue == null ? "" : nodeValue);
    }
}
