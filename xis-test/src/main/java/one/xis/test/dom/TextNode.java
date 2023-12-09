package one.xis.test.dom;

import lombok.Getter;

public class TextNode extends Node {

    @Getter
    public Object nodeValue;

    @SuppressWarnings("unused") // used in js
    public static final int nodeType = 3;
    public Object _expression;

    public TextNode(Object nodeValue) {
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
    protected void evaluateContent(StringBuilder builder, int indent) {
        if (nodeValue != null) {
            builder.append(nodeValue);
        }
    }

    @Override
    public String toString() {
        return "TextNode(" + nodeValue + ")";
    }


    @SuppressWarnings("unused")
    public void setNodeValue(Object nodeValue) {
        this.nodeValue = nodeValue;
        parentNode.textNodeChanged();
    }

    public void nodeValueChanged() {
        parentNode.textNodeChanged();
    }
}
