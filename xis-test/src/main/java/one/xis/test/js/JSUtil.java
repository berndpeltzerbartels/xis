package one.xis.test.js;

import lombok.experimental.UtilityClass;

import javax.script.*;
import java.io.Writer;
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
        var compiler = (Compilable) engine;
        var compiledScript = compiler.compile(javascript);
        engine.getContext().setErrorWriter(new ExceptionThrowingErrorWriter());
        return compiledScript;
    }

    public Object execute(String javascript, Map<String, Object> bindingMap) throws ScriptException {
        return compile(javascript, bindingMap).eval();
    }


    public Object execute(String js) throws ScriptException {
        return compile(js).eval();
    }

    class ExceptionThrowingErrorWriter extends Writer {

        private final StringBuilder stringBuilder = new StringBuilder();

        @Override
        public void write(char[] cbuf, int off, int len) {
            var s = new String(cbuf, off, len);
            stringBuilder.append(s);
            System.err.print(s);
        }

        @Override
        public void flush() {
            throw new RuntimeException(stringBuilder.toString());
        }

        @Override
        public void close() {
            throw new RuntimeException(stringBuilder.toString());
        }
    }

}
