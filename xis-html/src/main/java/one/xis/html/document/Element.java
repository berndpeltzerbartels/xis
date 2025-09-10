package one.xis.html.document;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class Element implements Node {
    private final String localName;
    private Node firstChild;
    private Element parentNode;
    private Node nextSibling;
    private final Map<String, String> attributes = new LinkedHashMap<>();

    @Override
    public String toHtml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<").append(localName);
        attributes.forEach((k, v) -> sb.append(" ").append(k).append("=\"").append(v).append("\""));
        if (SelfClosingTags.isSelfClosing(localName)) {
            if (firstChild != null) {
                throw new IllegalStateException("Self-closing tag <" + localName + "> cannot have children");
            }
            sb.append(">");
            return sb.toString();
        }
        sb.append(">");
        Node child = firstChild;
        while (child != null) {
            sb.append(child.toHtml());
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
