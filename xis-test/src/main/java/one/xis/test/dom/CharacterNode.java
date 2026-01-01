package one.xis.test.dom;

import lombok.Getter;

/**
 * Base class for character data nodes (TextNode and CommentNode).
 * Both share common properties: nodeValue and data.
 */
@Getter
public abstract class CharacterNode extends NodeImpl {

    private String nodeValue;

    @SuppressWarnings("unused") // used in js via GraalVM
    public String data;

    protected CharacterNode(int nodeType, String nodeValue) {
        super(nodeType);
        this.nodeValue = nodeValue;
        this.data = nodeValue;
    }

    @SuppressWarnings("unused") // used in js via GraalVM
    public void setNodeValue(String nodeValue) {
        this.nodeValue = nodeValue;
        this.data = nodeValue;
    }

    @SuppressWarnings("unused") // used in js via GraalVM
    public void setData(String data) {
        this.data = data;
        this.nodeValue = data;
    }

    @Override
    public String asString() {
        return nodeValue != null ? nodeValue : "";
    }

    @Override
    protected void evaluateContent(StringBuilder builder, int indent) {
        builder.append(nodeValue == null ? "" : nodeValue);
    }
}
