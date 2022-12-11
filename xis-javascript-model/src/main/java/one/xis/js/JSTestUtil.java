package one.xis.js;

import lombok.experimental.UtilityClass;
import one.xis.utils.io.IOUtils;

import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.ScriptException;

@UtilityClass
public class JSTestUtil {

    private static final String LOCAL_STORAGE = "class LocalStorage { getItem(key){} setItem(key, value) {} }; localStorage = new LocalStorage();";

    public CompiledScript compileWithApi(String javascript) throws ScriptException {
        return compileWithApi(javascript, null);
    }

    public CompiledScript compileWithApi(String javascript, Bindings bindings) throws ScriptException {
        var api = IOUtils.getResourceAsString("js/xis.js");
        var globals = IOUtils.getResourceAsString("js/xis-globals.js");
        return JSUtil.compile(LOCAL_STORAGE + api + globals + javascript, bindings);
    }

}
