package one.xis.remote.js;

import java.io.PrintWriter;

public class JSCode implements JSStatement {
    private final String js;

    public JSCode(String... code) {
        js = String.join("", code);
    }

    @Override
    public void writeJS(PrintWriter writer) {
        writer.append(js);
    }
}
