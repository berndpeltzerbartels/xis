package one.xis.remote.js;

import lombok.NonNull;

import java.io.PrintWriter;

public class JSVarDeclaration implements JSStatement {
    private final JSVar jsVar;
    private final JSValue value;
    private final JSMethodCall methodCall;
    private final String jsCode;

    public JSVarDeclaration(@NonNull JSVar jsVar, @NonNull JSValue value) {
        this.jsVar = jsVar;
        this.value = value;
        this.methodCall = null;
        this.jsCode = null;
    }

    public JSVarDeclaration(@NonNull JSVar jsVar, @NonNull JSMethodCall methodCall) {
        this.jsVar = jsVar;
        this.value = null;
        this.methodCall = methodCall;
        this.jsCode = null;
    }

    public JSVarDeclaration(@NonNull JSVar jsVar, @NonNull String jsCode) {
        this.jsVar = jsVar;
        this.value = null;
        this.methodCall = null;
        this.jsCode = jsCode;
    }

    @Override
    public void writeJS(PrintWriter writer) {
        writer.write("var ");
        writer.write(jsVar.getName());
        writer.write("=");
        if (value != null) {
            writer.write(value.getName());
        } else if (methodCall != null) {
            methodCall.writeJS(writer);
        } else if (jsCode != null) {
            writer.append(jsCode);
        } else {
            throw new IllegalStateException();
        }
    }
}
