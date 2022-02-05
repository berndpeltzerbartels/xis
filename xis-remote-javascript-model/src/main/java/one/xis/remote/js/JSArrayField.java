package one.xis.remote.js;

import java.io.PrintWriter;


public class JSArrayField extends JSField implements JSArray {

    public JSArrayField(String name, String defaultValue) {
        super(name, defaultValue);
    }

    public JSArrayField(String name) {
        super(name, "[]");
    }

    @Override
    public void writeJS(PrintWriter writer) {
        writer.append(name);
        writer.append(":");
        writer.append(defaultValue);
    }

    @Override
    public void writeReferenceJS(PrintWriter writer) {
        writer.append("this.");
        writer.append(name);
    }
}
