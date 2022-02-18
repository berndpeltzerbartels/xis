package one.xis.js;

import lombok.Value;

@Value
public class JSPlusEquals implements JSStatement {
    JSVariable variable;
    JSValue value;

}
