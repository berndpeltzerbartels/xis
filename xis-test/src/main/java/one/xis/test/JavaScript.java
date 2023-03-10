package one.xis.test;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import one.xis.resource.ClassPathResource;
import one.xis.test.js.JSUtil;
import one.xis.test.mocks.Console;
import one.xis.test.mocks.Document;
import one.xis.test.mocks.LocalStorage;

import javax.script.CompiledScript;
import javax.script.ScriptException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class JavaScript {

    private final CompiledScript compiledScript;
    private final Document document;
    private final Console console;
    private final LocalStorage localStorage;
    private final Map<String, Object> bindings;

    public <T> T run(Class<T> clazz) throws ScriptException {
        return clazz.cast(compiledScript.eval());
    }

    @SuppressWarnings("unchecked")
    public <T> void assertResultEquals(T o) throws ScriptException {
        var result = (T) runForObject();
        if (!Objects.equals(o, result)) {
            throw new AssertionError(String.format("Result was %s instead of %s", result, o));
        }
    }

    public Object runForObject() throws ScriptException {
        return compiledScript.eval();
    }


    public <T> T runForObject(Class<T> clazz) throws ScriptException {
        return clazz.cast(compiledScript.eval());
    }


    public Map<String, Object> runForMap() throws ScriptException {
        return run(Map.class);
    }


    public static JavaScriptBuilder builder() {
        return new JavaScriptBuilder();
    }

    @NoArgsConstructor(access = AccessLevel.PACKAGE)
    public static class JavaScriptBuilder {

        private boolean withApi;
        private final Document document = new Document();
        private final Console console = new Console();
        private final LocalStorage localStorage = new LocalStorage();
        private final StringBuilder userScript = new StringBuilder();
        private final Map<String, Object> bindings = new HashMap<>();

        public JavaScriptBuilder withApi() {
            withApi = true;
            return this;
        }

        public JavaScriptBuilder withScript(String script) {
            userScript.append(script);
            userScript.append("\n");
            return this;
        }

        public JavaScriptBuilder withBinding(String name, Object value) {
            bindings.put(name, value);
            return this;
        }

        public JavaScriptBuilder withHtmlMocks() {
            bindings.put("localStorage", localStorage);
            bindings.put("document", document);
            bindings.put("console", console);
            return this;
        }

        public JavaScript build() throws ScriptException {
            StringBuilder script = new StringBuilder();
            if (withApi) {
                script.append(new ClassPathResource("js/xis.js").getContent());
            }
            script.append(userScript);
            return new JavaScript(JSUtil.compile(script.toString(), bindings), document, console, localStorage, bindings);
        }
    }

}
