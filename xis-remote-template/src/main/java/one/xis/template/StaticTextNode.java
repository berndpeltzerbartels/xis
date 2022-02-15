package one.xis.template;

import lombok.Data;

@Data
class StaticTextNode implements TextNode {
    private final String content;
}
