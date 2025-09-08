package one.xis.html.parts;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class Tag implements Part {
    private final String localName;
    private boolean openTag; // false = closingTag
    private boolean empty;
    private Map<String, String> attributes = new HashMap<>();

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<");
        if (!openTag) {
            sb.append("/");
        }
        sb.append(localName);
        attributes.forEach((k, v) -> sb.append(" ").append(k).append("=\"").append(v).append("\""));
        if (openTag && empty) {
            sb.append(" /");
        }
        sb.append(">");
        return sb.toString();
    }
}
