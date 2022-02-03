package one.xis.remote.js;

import java.io.PrintWriter;

public class JSFieldAssigment implements JSStatement {
    private final JSField field;
    private final JSValue value;
    private final JSMethodCall methodCall;

    public JSFieldAssigment(JSField field, JSValue value) {
        this.field = field;
        this.value = value;
        this.methodCall = null;
    }


    public JSFieldAssigment(JSField field, JSMethodCall methodCall) {
        this.field = field;
        this.value = null;
        this.methodCall = methodCall;
    }

    @Override
    public void writeJS(PrintWriter writer) {
        writer.write("this.");
        writer.write(field.getName());
        writer.write("=");
        if (value != null) {
            writer.write(value.getName());
        } else if (methodCall != null) {
            methodCall.writeJS(writer);
        }
        writer.write(";");
    }
}
