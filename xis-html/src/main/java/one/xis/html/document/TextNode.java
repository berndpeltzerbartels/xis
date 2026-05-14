package one.xis.html.document;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@EqualsAndHashCode
public class TextNode implements Node {
    private final StringBuilder text;
    private Node nextSibling;
    private Element parentNode;

    public TextNode(String text) {
        this.text = new StringBuilder(text);
    }

    public void append(String s) {
        text.append(s);
    }

    public String getText() {
        return text.toString();
    }

    @Override
    public String toHtml() {
        return text.toString();
    }

    @Override
    public int getNodeType() {
        return 3; // TEXT_NODE
    }
}
