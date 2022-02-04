package one.xis.remote.js;

import lombok.NonNull;

import java.io.PrintWriter;

public class JSAssigment implements JSStatement {
    private final JSValue target;
    private final JSValue value;
    private final JSMethodCall methodCall;
    private final String code;

    public JSAssigment(@NonNull JSValue target, @NonNull JSValue value) {
        this.target = target;
        this.value = value;
        this.methodCall = null;
        this.code = null;
    }

    public JSAssigment(@NonNull JSValue target, @NonNull JSMethodCall methodCall) {
        this.target = target;
        this.value = null;
        this.methodCall = methodCall;
        this.code = null;
    }

    public JSAssigment(@NonNull JSValue target, @NonNull String code) {
        this.target = target;
        this.value = null;
        this.methodCall = null;
        this.code = code;
    }

    @Override
    public void writeJS(PrintWriter writer) {
        target.writeReferenceJS(writer);
        writer.append("=");
        if (value != null) {
            writer.write(value.getName());
        } else if (methodCall != null) {
            methodCall.writeJS(writer);
        } else if (code != null) {
            writer.write(code);
        } else {
            throw new IllegalStateException();
        }
    }
}
