package one.xis.html.parts;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class Tag implements Part {
    private final String localName;
    private TagType tagType;
    private Map<String, String> attributes = new LinkedHashMap<>();

    public boolean isEmpty() {
        return tagType == TagType.NO_CONTENT;
    }

    public boolean isOpening() {
        return tagType == TagType.OPENING;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<");
        if (tagType == TagType.CLOSING) {
            sb.append("/");
        }
        sb.append(localName);
        attributes.forEach((k, v) -> sb.append(" ").append(k).append("=\"").append(v).append("\""));
        if (tagType == TagType.NO_CONTENT) {
            sb.append(" /");
        }
        sb.append(">");
        return sb.toString();
    }
}
