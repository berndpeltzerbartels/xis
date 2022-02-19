package one.xis.js;

import lombok.Value;

@Value
public class JSVarAssignment implements JSStatement {
    JSVar jsVar;
    JSValue value;
}
