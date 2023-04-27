package one.xis.context;


import lombok.Getter;
import one.xis.resource.Resources;
import one.xis.server.FrontendService;
import one.xis.test.dom.Document;
import one.xis.test.dom.Element;
import one.xis.test.dom.Window;
import one.xis.test.js.JSUtil;
import one.xis.test.js.LocalStorage;

import javax.script.CompiledScript;
import javax.script.ScriptException;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.function.BiFunction;

@SuppressWarnings("unused")
public class IntegrationTestContext {

    @Getter
    private final Document document;

    @Getter
    private final LocalStorage localStorage;

    @Getter
    private final Window window;
    private final CompiledScript compiledScript;
    private final FrontendService frontendService;


    public static Builder builder() {
        return new Builder();
    }

    public IntegrationTestContext(Object... controllers) {
        this.localStorage = new LocalStorage();
        var resources = new Resources();
        frontendService = internalContext(controllers).getSingleton(FrontendService.class);
        document = Document.of(frontendService.getRootPageHtml());
        window = new Window();
        var script = resources.getByPath("xis.js").getContent() + "\n" + START_SCRIPT;
        compiledScript = compileScript(script);
    }

    public void openPage(String uri, Map<String, Object> parameters) {
        document.location.pathname = uri;
        try {
            compiledScript.eval();
        } catch (ScriptException e) {
            throw new RuntimeException("Compilation failed :" + e.getMessage() + " at line " + e.getLineNumber() + ", column " + e.getColumnNumber());
        }
    }

    public void openPage(String uri) {
        openPage(uri, Collections.emptyMap());
    }

    class ErrorWriter extends Writer {

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            System.err.print(new String(cbuf, off, len));
        }

        @Override
        public void flush() throws IOException {
            System.err.println();
        }

        @Override
        public void close() throws IOException {
            System.err.println();
        }
    }

    public Element htmlToElement(String name, String content) {
        var doc = Document.of(new StringBuilder().append("<").append(name).append(">")
                .append(content)
                .append("</").append(name).append(">").toString());
        return doc.rootNode;
    }

    private CompiledScript compileScript(String javascript) {
        var bindings = new HashMap<String, Object>();
        bindings.put("controllerBridge", new ControllerBridge(frontendService));
        bindings.put("localStorage", localStorage);
        bindings.put("document", document);
        bindings.put("window", window);
        BiFunction<String, String, Element> bind = this::htmlToElement;
        bindings.put("htmlToElement", bind);
        try {
            return JSUtil.compile(javascript, bindings);
        } catch (ScriptException e) {
            throw new RuntimeException("Compilation failed :" + e.getMessage() + " at line " + e.getLineNumber() + ", column " + e.getColumnNumber());
        }
    }

    private AppContext internalContext(Object... controllers) {
        return AppContextBuilder.createInstance()
                .withPackage("one.xis")
                .withSingeltons(controllers)
                .build();
    }

    public static class Builder {

        private final Collection<Object> singeltons = new HashSet<>();

        public Builder withSingelton(Object o) {
            singeltons.add(o);
            return this;
        }

        public Builder withMock(Object o) {
            return withSingelton(o);
        }

        public IntegrationTestContext build() {
            return new IntegrationTestContext(singeltons.stream().toArray(Object[]::new));
        }
    }

    private static final String START_SCRIPT = "var httpClient = new HttpClientMock(controllerBridge);\n" +
            "var starter = new Starter(httpClient);\n" +
            "starter.doStart();";


}
