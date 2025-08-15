package one.xis.test.dom;

import lombok.Getter;
import org.graalvm.polyglot.Value;

import java.util.List;


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
    public NodeImpl cloneNode() {
        return new TextNodeIml(nodeValue);
    }

    @Override
    public String getName() {
        return "TextNode";
    }

    @Override
    public String asString() {
        return nodeValue != null ? nodeValue.toString() : "";
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
    public void setNodeValue(String nodeValue) {
        this.nodeValue = nodeValue;
    }

    @Override
    public Object getMember(String key) {
        if ("nodeValue".equals(key)) {
            return nodeValue;
        }
        throw new IllegalArgumentException("Unknown member: " + key);
    }

    @Override
    public Object getMemberKeys() {
        return List.of("nodeValue");
    }

    @Override
    public boolean hasMember(String key) {
        return "nodeValue".equals(key);
    }

    @Override
    public void putMember(String key, Value value) {
        if ("nodeValue".equals(key)) {
            setNodeValue(value.asString());
        } else {
            throw new IllegalArgumentException("Unknown member: " + key);
        }
    }
}
