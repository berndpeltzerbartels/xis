package one.xis.remote.js;

import java.io.PrintWriter;

public class JSVarDeclaration implements JSStatement {
    private final JSVar jsVar;
    private final JSValue value;
    private final JSMethodCall methodCall;

    public JSVarDeclaration(JSVar jsVar, JSValue value) {
        this.jsVar = jsVar;
        this.value = value;
        this.methodCall = null;
    }

    public JSVarDeclaration(JSVar jsVar, JSMethodCall methodCall) {
        this.jsVar = jsVar;
        this.value = null;
        this.methodCall = methodCall;
    }


    @Override
    public void writeJS(PrintWriter writer) {
        writer.write("this.");
        writer.write(jsVar.getName());
        writer.write("=");
        if (value != null) {
            writer.write(value.getName());
        } else if (methodCall != null) {
            methodCall.writeJS(writer);
        }
        writer.write(";");
    }
}
