package one.xis.js;

import lombok.Data;

@Data
public class JSMethodCall implements JSContext, JSValue, JSStatement {
    private JSVar owner;
    private JSMethod method;
    private JSValue[] args;

    public JSMethodCall(JSVar owner, JSMethod method, JSValue... args) {
        this.owner = owner;
        this.method = method;
        this.args = args;
        validateArgs();
    }

    public JSMethodCall(JSMethod method, JSValue... args) {
        this.owner = null;
        this.method = method;
        this.args = args;
        validateArgs();
    }


    private void validateArgs() {
        JSMethod declaredMethod = method.getOwner().getMethod(method.getName());
        if (args.length != declaredMethod.getArgs().size()) {
            throw new IllegalStateException(String.format("method %s in %s requires %d args instead of %d", declaredMethod.getName(), declaredMethod.getOwner().getClassName(), declaredMethod.getArgs(), args.length));
        }
    }
}
