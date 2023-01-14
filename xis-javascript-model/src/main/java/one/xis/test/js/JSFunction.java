package one.xis.test.js;

import lombok.Value;

@Value
public class JSFunction implements JSDeclaration, JSContext {
    String name;
    int minArgs;
    int maxArgs;
}
