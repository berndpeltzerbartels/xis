package one.xis.remote.js;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class JSMethodCall implements JSStatement {
    private final JSObjectInstance objectInstance;
    private final JSMethod method;
    private final List<JSValue> args;

    public JSMethodCall(JSObjectInstance objectInstance, JSMethod method, JSValue... args) {
        this.objectInstance = objectInstance;
        this.method = method;
        this.args = Arrays.asList(args);
    }

    @Override
    public void writeJS(PrintWriter writer) {
        writer.write(objectInstance.getName());
        writer.write(".");
        writer.write(method.getName());
        writer.write("(");
        writer.write(args.stream().map(JSValue::getName).collect(Collectors.joining(",")));
        writer.write(")");
    }
}
