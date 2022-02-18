package one.xis.js;

import lombok.Value;

@Value
public class JSCode implements JSStatement, JSValue {

    StringBuilder code = new StringBuilder();

    public JSCode append(Object o) {
        code.append(o);
        return this;
    }
}
