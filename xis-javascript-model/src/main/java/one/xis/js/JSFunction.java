package one.xis.js;

import lombok.Value;

@Value
public class JSFunction implements JSDeclaration {
    String name;
    int minArgs;
    int maxArgs;
}
