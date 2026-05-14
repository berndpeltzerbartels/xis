package one.xis.test.dom;

/**
 * Represents a comment node in the DOM.
 * Comments are included in childNodes but their content is ignored by textContent/innerText.
 */
public class CommentNodeImpl extends CharacterNode implements CommentNode {

    @SuppressWarnings("unused") // used in js via GraalVM
    public static final int nodeType = 8;

    public CommentNodeImpl(String nodeValue) {
        super(COMMENT_NODE, nodeValue);
    }

    @Override
    public Node cloneNode() {
        return new CommentNodeImpl(getNodeValue());
    }

    @Override
    public String toString() {
        var value = getNodeValue() == null ? "" : getNodeValue();
        return "CommentNode(" + value + ")";
    }

    @Override
    protected void evaluateContent(StringBuilder builder, int indent) {
        // Comments werden im HTML-Output als <!-- ... --> dargestellt
        builder.append("<!--").append(getNodeValue() == null ? "" : getNodeValue()).append("-->");
    }
}
