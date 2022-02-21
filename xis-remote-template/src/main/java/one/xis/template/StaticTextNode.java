package one.xis.template;

import lombok.Data;

@Data
public class StaticTextNode implements TextNode {
    private final String content;
}
