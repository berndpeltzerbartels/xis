package one.xis.remote.js;

import lombok.Data;

import java.io.PrintWriter;

@Data
public class JSField implements JSValue {
    private final String name;
    private final String defaultValue;

    @Override
    public void writeJS(PrintWriter writer) {
        writer.append(name);
        writer.append(":");
        writer.append(defaultValue);
        writer.append(";");
    }

    @Override
    public void writeReferenceJS(PrintWriter writer) {
        writer.append("this.");
        writer.append(name);
    }
}
