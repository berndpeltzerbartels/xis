package one.xis.html.document;

import lombok.Data;


@Data
public class TextNode implements Node {
    private final String text;
    private Node nextSibling;
    private Element parentNode;

    @Override
    public String toHtml() {
        return text;
    }

    @Override
    public int getNodeType() {
        return 3; // TEXT_NODE
    }
}
