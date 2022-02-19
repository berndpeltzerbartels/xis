package one.xis.js;

import lombok.Value;

@Value
public class JSMethodCall implements JSContext, JSValue, JSStatement {
    JSContext parent;
    JSMethod method;
    JSValue[] args;

    public JSMethodCall(JSContext parent, JSMethod method, JSValue... args) {
        this.parent = parent;
        this.method = method;
        this.args = args;
    }
}
