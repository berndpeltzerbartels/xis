package one.xis.remote.js;

import lombok.Data;

import java.io.PrintWriter;

@Data
public class JSVar implements JSValue {
    private final String name;

    @Override
    public void writeJS(PrintWriter writer) {
        writer.append(name);
    }

    @Override
    public void writeReferenceJS(PrintWriter writer) {
        writer.append(name);
    }
}
