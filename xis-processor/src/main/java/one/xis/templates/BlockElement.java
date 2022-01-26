package one.xis.templates;

import lombok.Data;

import java.util.Map;

@Data
class BlockElement {
    private final String tagName;
    private Map<String, String> attributes;
}
