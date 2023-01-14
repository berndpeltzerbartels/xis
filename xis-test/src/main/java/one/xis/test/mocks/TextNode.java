package one.xis.test.mocks;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class TextNode extends Node {
    private final String text;
}
