package one.xis.test.js;

import lombok.Value;

@Value
class JSStringAppend implements JSStatement {
    JSVar variable;
    JSValue value;
}
