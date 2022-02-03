package one.xis.remote.js;

import java.io.PrintWriter;

public interface JSValue extends JSElement {
    String getName();

    void writeReferenceJS(PrintWriter writer);
}
