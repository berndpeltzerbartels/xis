package one.xis.js;

import lombok.experimental.UtilityClass;

import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.ScriptException;

import static one.xis.utils.io.IOUtils.getResourceAsString;

@UtilityClass
public class JSTestUtil {

    private static final String LOCAL_STORAGE = "class LocalStorage { getItem(key){} setItem(key, value) {} }; localStorage = new LocalStorage();";

    public CompiledScript compileWithApi(String javascript) throws ScriptException {
        return compileWithApi(javascript, null);
    }

    public CompiledScript compileWithApi(String javascript, Bindings bindings) throws ScriptException {
        var api = getResourceAsString("js/xis.js");
        var globals = getResourceAsString("js/xis-globals.js");
        return JSUtil.compile(LOCAL_STORAGE + api + globals + javascript, bindings);
    }

    public CompiledScript compile(String javascript) throws ScriptException {
        return compile(javascript, null);
    }

    public CompiledScript compile(String javascript, Bindings bindings) throws ScriptException {
        var api = getResourceAsString("js/xis.js");
        var globals = getResourceAsString("js/xis-globals.js");
        return JSUtil.compile(javascript, bindings);
    }
    

}
