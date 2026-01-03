package one.xis.html.parts;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class OpeningTag implements Tag, OpeningNode {
    private final String localName;
    private int tokenCount;
    private Map<String, String> attributes = new LinkedHashMap<>();


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<");
        sb.append(localName);
        attributes.forEach((k, v) -> sb.append(" ").append(k).append("=\"").append(v).append("\""));
        sb.append(">");
        return sb.toString();
    }

    @Override
    public int tokenCount() {
        return tokenCount;
    }

    void addTokenCount(int count) {
        this.tokenCount += count;
    }
}
