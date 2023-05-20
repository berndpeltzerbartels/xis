package one.xis.test.js;

import lombok.experimental.UtilityClass;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotAccess;

import javax.script.*;
import java.io.Writer;
import java.util.Map;

import static java.util.Collections.emptyMap;

@UtilityClass
public class JSUtil {

    public CompiledScript compile(String javascript) throws ScriptException {
        return compile(javascript, emptyMap());
    }

    public CompiledScript compile(String javascript, Map<String, Object> bindingMap) throws ScriptException {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("graal.js");
        //engine.put("bindToElement", (BiFunction<String, String, Element>) (s, s2) -> new Element(s2));
        //engine.getContext().setAttribute("bindToElement", (BiFunction<String, String, Element>) (s, s2) -> new Element(s2), ScriptContext.ENGINE_SCOPE);
        Bindings bindings = engine.createBindings();
        bindings.put("polyglot.js.allowHostClassLookup", true);
        bindings.put("polyglot.js.allowAllAccess", true);
        bindings.put("polyglot.inspect", 9229);
        bindings.put("engine.WarnInterpreterOnly", false);
        bindings.putAll(bindingMap);
        engine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
        //System.out.println(javascript);
        var compiler = (Compilable) engine;
        var compiledScript = compiler.compile(javascript);
        engine.getContext().setErrorWriter(new ExceptionThrowingErrorWriter());
        return compiledScript;
    }

    public Object execute(String javascript, Map<String, Object> bindingMap) throws ScriptException {
        if ("true".equals(System.getenv().get("debug")) || "true".equals(System.getProperty("debug"))) {
            return debug(javascript, bindingMap);
        }
        return compile(javascript, bindingMap).eval();
    }


    public Object execute(String js) throws ScriptException {
        if ("true".equals(System.getenv().get("debug")) || "true".equals(System.getProperty("debug"))) {
            return debug(js, emptyMap());
        }
        return compile(js).eval();
    }

    public Object debug(String js, Map<String, Object> bindingMap) {
        String port = "4242";
        String path = "xis";
        Context context = Context.newBuilder("js")
                .allowAllAccess(true)
                .allowExperimentalOptions(true)
                .allowHostClassLoading(true)
                .allowHostClassLookup(c -> true)
                .allowPolyglotAccess(PolyglotAccess.ALL)
                .allowNativeAccess(true)
                .option("inspect", port)
                .option("inspect.Secure", "false")
                .option("inspect.Path", path)
                .build();
        bindingMap.forEach(context.getBindings("js")::putMember);
        return context.eval("js", js);
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
