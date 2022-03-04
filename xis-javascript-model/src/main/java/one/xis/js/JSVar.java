package one.xis.js;

import lombok.Value;

@Value
public class JSVar implements JSVariable, JSContext {
    String name;
}
