package one.xis.js;

import lombok.Value;

@Value
public class JSMethodCall implements JSContext, JSValue, JSStatement {
    JSVar owner;
    JSMethod method;
    JSValue[] args;

    public JSMethodCall(JSVar owner, JSMethod method, JSValue... args) {
        this.owner = owner;
        this.method = method;
        this.args = args;
        // TODO validate number of args
    }

    public JSMethodCall(JSMethod method, JSValue... args) {
        this.owner = null;
        this.method = method;
        this.args = args;
        // TODO validate number of args
    }
}
