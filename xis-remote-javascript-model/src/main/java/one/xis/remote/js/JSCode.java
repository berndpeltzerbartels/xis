package one.xis.remote.js;

import lombok.RequiredArgsConstructor;

import java.io.PrintWriter;

@RequiredArgsConstructor
public class JSCode implements JSStatement {
    private final String js;

    @Override
    public void writeJS(PrintWriter writer) {
        writer.append(js);
    }
}
