package one.xis.js;

import lombok.Value;

@Value
class JSStringAppend implements JSStatement {
    JSVar variable;
    JSValue value;
}
