package one.xis.html.document;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.List;
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

    public String getTagName() {
        return localName.toUpperCase();
    }

    public void setAttribute(String name, String value) {
        attributes.put(name, value);
    }


    public Element getElementByTagName(String tagName) {
        if (this.localName.equalsIgnoreCase(tagName)) {
            return this;
        }
        Node child = firstChild;
        while (child != null) {
            if (child instanceof Element) {
                Element found = ((Element) child).getElementByTagName(tagName);
                if (found != null) {
                    return found;
                }
            }
            child = child.getNextSibling();
        }
        return null;
    }

    public void appendChild(Node newChild) {
        if (newChild instanceof Element) {
            ((Element) newChild).setParentNode(this);
        }
        if (firstChild == null) {
            firstChild = newChild;
        } else {
            Node last = firstChild;
            while (last.getNextSibling() != null) {
                last = last.getNextSibling();
            }
            last.setNextSibling(newChild);
        }
    }

    public List<Element> getElementsByTagName(String name) {
        List<Element> result = new java.util.ArrayList<>();
        if (this.localName.equalsIgnoreCase(name)) {
            result.add(this);
        }
        Node child = firstChild;
        while (child != null) {
            if (child instanceof Element) {
                result.addAll(((Element) child).getElementsByTagName(name));
            }
            child = child.getNextSibling();
        }
        return result;
    }
}
