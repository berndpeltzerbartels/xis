package one.xis.template;

import lombok.Data;

import java.util.List;

@Data
class TextNode implements ModelNode {
    private final List<MixedContent> contents;
}
