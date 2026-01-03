package one.xis.html.parts;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class ClosingTag implements Tag {
    private final String localName;
    private Map<String, String> attributes = new LinkedHashMap<>();

    @Override
    public String toString() {
        return "</" +
                localName +
                ">";
    }

    @Override
    public int tokenCount() {
        return 4;
    }
}
