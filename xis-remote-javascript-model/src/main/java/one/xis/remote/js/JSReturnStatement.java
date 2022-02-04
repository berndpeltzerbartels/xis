package one.xis.remote.js;

import lombok.Data;

import java.io.PrintWriter;

@Data
public class JSReturnStatement implements JSStatement {
    private final JSValue value;

    @Override
    public void writeJS(PrintWriter writer) {
        writer.write("return ");
        writer.write(value.getName());
    }
}
