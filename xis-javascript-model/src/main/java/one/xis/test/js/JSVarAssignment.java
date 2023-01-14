package one.xis.test.js;

import lombok.Value;

@Value
public class JSVarAssignment implements JSStatement {
    JSVar jsVar;
    JSValue value;
}
