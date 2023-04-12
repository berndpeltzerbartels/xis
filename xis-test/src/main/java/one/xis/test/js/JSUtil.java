package one.xis.test.js;

import lombok.experimental.UtilityClass;

import javax.script.*;
import java.util.Collections;
import java.util.Map;

@UtilityClass
public class JSUtil {

    public CompiledScript compile(String javascript) throws ScriptException {
        return compile(javascript, Collections.emptyMap());
    }

    public CompiledScript compile(String javascript, Map<String, Object> bindingMap) throws ScriptException {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("graal.js");
        Bindings bindings = engine.createBindings();
        bindings.put("polyglot.js.allowHostClassLookup", true);
        bindings.put("polyglot.js.allowAllAccess", true);
        bindings.putAll(bindingMap);
        engine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);

        Compilable compiler = (Compilable) engine;
        return compiler.compile(javascript);
    }

    public Object execute(String javascript, Map<String, Object> bindingMap) throws ScriptException {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("graal.js");
        Bindings bindings = engine.createBindings();
        bindings.put("polyglot.js.allowHostClassLookup", true);
        bindings.put("polyglot.js.allowAllAccess", true);
        bindings.putAll(bindingMap);
        engine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
        return engine.eval(javascript);
    }


    public Object execute(String js) throws ScriptException {
        return compile(js).eval();
    }

}
