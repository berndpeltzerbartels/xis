package one.xis.html.document;

import lombok.Data;

@Data
public class Element implements Node {
    private final String localName;
    private Node firstChild;
    private Element parentNode;
    private Node nextSibling;

    @Override
    public String asString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<").append(localName).append(">");
        Node child = firstChild;
        while (child != null) {
            sb.append(child.asString());
            child = child.getNextSibling();
        }
        sb.append("</").append(localName).append(">");
        return sb.toString();
    }

    @Override
    public String toString() {
        return "<" + localName + ">";
    }
}
