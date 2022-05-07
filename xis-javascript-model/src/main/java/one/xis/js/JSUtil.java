package one.xis.js;

import lombok.experimental.UtilityClass;

import javax.script.*;

@UtilityClass
public class JSUtil {

    public CompiledScript compile(String javascript) throws ScriptException {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("graal.js");
        Compilable compiler = (Compilable) engine;
        return compiler.compile(javascript);
    }

    public Object execute(String js) throws ScriptException {
        return compile(js).eval();
    }
}
