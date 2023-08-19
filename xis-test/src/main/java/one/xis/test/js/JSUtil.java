package one.xis.test.js;

import lombok.experimental.UtilityClass;
import one.xis.context.JavascriptFunction;
import one.xis.context.JavascriptFunctionContext;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotAccess;
import org.graalvm.polyglot.Value;

import javax.script.ScriptException;
import java.io.Writer;
import java.util.Map;

import static java.util.Collections.emptyMap;

@UtilityClass
public class JSUtil {

    public JavascriptFunction function(String script, Map<String, Object> bindings) {
        Context context;
        if ("true".equals(System.getenv().get("debug")) || "true".equals(System.getProperty("debug"))) {
            context = debugContext(bindings);
        } else {
            context = runContext(bindings);
        }
        var value = context.eval("js", script);
        return new JavascriptFunctionContext(value, context);
    }


    public Value execute(String js, Context context) {
        return context.eval("js", js);
    }

    public Value execute(String javascript, Map<String, Object> bindingMap) throws ScriptException {
        if ("true".equals(System.getenv().get("debug")) || "true".equals(System.getProperty("debug"))) {
            return debug(javascript, bindingMap);
        }
        return doRun(javascript, bindingMap);
    }

    public <T> T execute(String javascript, Class<T> clazz) throws ScriptException {
        return execute(javascript, emptyMap(), clazz);
    }

    public <T> T execute(String javascript, Map<String, Object> bindingMap, Class<T> clazz) throws ScriptException {
        if ("true".equals(System.getenv().get("debug")) || "true".equals(System.getProperty("debug"))) {
            return clazz.cast(debug(javascript, bindingMap));
        }
        return clazz.cast(doRun(javascript, bindingMap));
    }


    public Value execute(String js) throws ScriptException {
        if ("true".equals(System.getenv().get("debug")) || "true".equals(System.getProperty("debug"))) {
            return debug(js, emptyMap());
        }
        return doRun(js, emptyMap());
    }

    public Value debug(String js, Map<String, Object> bindingMap) {
        Context context = debugContext(bindingMap);
        bindingMap.forEach(context.getBindings("js")::putMember);
        return context.eval("js", js);
    }

    public Value doRun(String js, Map<String, Object> bindingMap) {
        Context context = runContext(bindingMap);
        bindingMap.forEach(context.getBindings("js")::putMember);
        return context.eval("js", js);
    }

    public Context context(Map<String, Object> bindings) {
        if ("true".equals(System.getenv().get("debug")) || "true".equals(System.getProperty("debug"))) {
            return debugContext(bindings);
        } else {
            return runContext(bindings);
        }
    }

    public Context debugContext(Map<String, Object> bindingMap) {
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
        return context;
    }


    public Context runContext(Map<String, Object> bindingMap) {
        Context context = Context.newBuilder("js")
                .allowAllAccess(true)
                .allowExperimentalOptions(true)
                .allowHostClassLoading(true)
                .allowHostClassLookup(c -> true)
                .allowPolyglotAccess(PolyglotAccess.ALL)
                .allowNativeAccess(true)
                .build();
        bindingMap.forEach(context.getBindings("js")::putMember);
        return context;
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
