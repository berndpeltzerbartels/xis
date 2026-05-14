package one.xis.js;

import javax.script.ScriptException;

public class TestJsException extends RuntimeException {

    public TestJsException(ScriptException e, String script) {
        super(createText(e, script));
        e.getSuppressed();
    }


    private static String createText(ScriptException e, String script) {
        String[] lines = script.split("\n");
        int first = Math.max(0, e.getLineNumber() - 3);
        int last = Math.min(lines.length - 1, e.getLineNumber() + 3);
        StringBuilder s = new StringBuilder();
        for (var i = first; i <= last; i++) {
            s.append(lines[i]);
            s.append("\n");
        }
        return s.toString();
    }
}
