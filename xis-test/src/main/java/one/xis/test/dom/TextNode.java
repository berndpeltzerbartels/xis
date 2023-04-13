package one.xis.test.dom;

import lombok.AllArgsConstructor;

@AllArgsConstructor
class TextNode extends Node {
    public String nodeValue;

    @Override
    public Node cloneNode() {
        return new TextNode(nodeValue);
    }

    @Override
    public String name() {
        return "TextNode";
    }
}
