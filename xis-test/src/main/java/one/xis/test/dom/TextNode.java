package one.xis.test.dom;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class TextNode extends Node {

    @Getter
    public String nodeValue;

    @Override
    public Node cloneNode() {
        return new TextNode(nodeValue);
    }

    @Override
    public String getName() {
        return "TextNode";
    }
}
