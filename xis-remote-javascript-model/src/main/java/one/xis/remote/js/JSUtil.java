package one.xis.remote.js;

import lombok.experimental.UtilityClass;

import javax.script.*;
import java.io.PrintWriter;
import java.io.StringWriter;

@UtilityClass
public class JSUtil {

    public CompiledScript compile(String javascript) throws ScriptException {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
        Compilable compiler = (Compilable) engine;
        return compiler.compile(javascript);
    }

    public Object execute(String js) throws ScriptException {
        return compile(js).eval();
    }

    public String javascript(JSElement element) {
        var writer = new StringWriter();
        element.writeJS(new PrintWriter(writer));
        return writer.toString();
    }

}
