package one.xis.remote.js;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class JSFunctionCall implements JSStatement {
    private final JSFunction function;
    private final List<JSValue> args;

    public JSFunctionCall(JSFunction function, JSValue... args) {
        this.function = function;
        this.args = Arrays.asList(args);
    }

    @Override
    public void writeJS(PrintWriter writer) {
        writer.write(function.getName());
        writer.write("(");
        writer.write(args.stream().map(JSValue::getName).collect(Collectors.joining(",")));
        writer.write(")");
    }
}
