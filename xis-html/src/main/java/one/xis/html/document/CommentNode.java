package one.xis.html.document;

import lombok.Data;

@Data
public class CommentNode implements Node {
    private final String text;
    private Node nextSibling;
    private Element parentNode;

    @Override
    public String toHtml() {
        return "<!--" + text + "-->";
    }

    @Override
    public int getNodeType() {
        return 8; // COMMENT_NODE
    }
}
