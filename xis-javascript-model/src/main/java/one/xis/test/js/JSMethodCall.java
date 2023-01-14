package one.xis.test.js;

import lombok.Data;

@Data
public class JSMethodCall implements JSContext, JSValue, JSStatement {
    private JSMethod method;
    private JSValue[] args;

    public JSMethodCall(JSMethod method, JSValue... args) {
        this.method = method;
        this.args = args;
        validateArgs();
    }


    private void validateArgs() {
        if (args.length != method.getArgs().size()) {
            throw new IllegalStateException(String.format("method %s in %s requires %d args instead of %d", method.getName(), method.getDeclaringClass().getClassName(), method.getArgs().size(), args.length));
        }
    }
}
