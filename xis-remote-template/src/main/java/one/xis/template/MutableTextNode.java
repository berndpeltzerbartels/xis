package one.xis.template;

import lombok.Data;

import java.util.List;

@Data
public class MutableTextNode implements TextNode, PathNode {
    private final List<MixedContent> content;
}
