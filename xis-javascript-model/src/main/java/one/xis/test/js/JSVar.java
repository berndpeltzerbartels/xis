package one.xis.test.js;

import lombok.Value;

@Value
public class JSVar implements JSVariable, JSContext {
    String name;
}
