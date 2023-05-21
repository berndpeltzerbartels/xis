package one.xis.context;


import lombok.Getter;
import lombok.NonNull;
import one.xis.resource.Resources;
import one.xis.server.FrontendService;
import one.xis.test.dom.Document;
import one.xis.test.dom.Element;
import one.xis.test.dom.TextNode;
import one.xis.test.dom.Window;
import one.xis.test.js.JSUtil;
import one.xis.test.js.LocalStorage;
import one.xis.utils.lang.StringUtils;

import javax.script.CompiledScript;
import javax.script.ScriptException;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.function.Function;

public class IntegrationTestContext {

    @Getter
    private final Document document;

    @Getter
    private final LocalStorage localStorage;

    @Getter
    private final Window window;
    private final String script;
    private final FrontendService frontendService;
    private final AppContext appContext;

    public static Builder builder() {
        return new Builder();
    }

    public IntegrationTestContext(Object... controllers) {
        this.localStorage = new LocalStorage();
        var resources = new Resources();
        appContext = internalContext(controllers);
        frontendService = appContext.getSingleton(FrontendService.class);
        document = Document.of(frontendService.getRootPageHtml());
        window = new Window();
        script = resources.getByPath("xis.js").getContent() + "\n" + START_SCRIPT;
    }

    public void openPage(String uri, Map<String, Object> parameters) {
        document.location.pathname = uri;
        try {
            JSUtil.execute(script, createBindings());
        } catch (ScriptException e) {
            throw new RuntimeException("Compilation failed :" + e.getMessage() + " at line " + e.getLineNumber() + ", column " + e.getColumnNumber());
        }
        finalizeDocument(document);
    }

    public void openPage(String uri) {
        openPage(uri, Collections.emptyMap());
    }

    public <T> T getSingleton(Class<T> type) {
        return appContext.getSingleton(type);
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

    public Element htmlToElement(String content) {
        var doc = Document.of(content);
        return doc.rootNode;
    }

    private CompiledScript compileScript(String javascript) {
        try {
            return JSUtil.compile(javascript, createBindings());
        } catch (ScriptException e) {
            throw new RuntimeException("Compilation failed :" + e.getMessage() + " at line " + e.getLineNumber() + ", column " + e.getColumnNumber());
        }
    }

    private Map<String, Object> createBindings() {
        var bindings = new HashMap<String, Object>();
        bindings.put("controllerBridge", new ControllerBridge(frontendService));
        bindings.put("localStorage", localStorage);
        bindings.put("document", document);
        bindings.put("window", window);
        Function<String, Element> htmlToElement = this::htmlToElement;
        bindings.put("htmlToElement", htmlToElement);
        return bindings;
    }

    private AppContext internalContext(Object... controllers) {
        return AppContextBuilder.createInstance()
                .withPackage("one.xis")
                .withSingletons(controllers)
                .build();
    }

    public static class Builder {

        private final Collection<Object> singletons = new HashSet<>();

        public Builder withSingleton(Object o) {
            singletons.add(o);
            return this;
        }

        public Builder withMock(Object o) {
            return withSingleton(o);
        }

        public IntegrationTestContext build() {
            return new IntegrationTestContext(singletons.toArray());
        }
    }

    private void finalizeDocument(@NonNull Document document) {
        if (document.rootNode != null) {
            finalizeElement(document.rootNode);
        }
    }

    private void finalizeElement(@NonNull Element element) {
        String value = null;
        if (element.getTextNode() != null) {
            value = StringUtils.toString(element.getTextNode().nodeValue);
        } else if (element.innerHTML != null) {
            value = element.innerHTML;
        } else if (element.innerText != null) {
            value = element.innerText;
        }
        element.innerHTML = value;
        element.innerText = value;
        if (element.getTextNode() == null) {
            var textNode = new TextNode(value);
            element.appendChild(textNode);
        }
        element.getChildElements().forEach(this::finalizeElement);
    }

    private static final String START_SCRIPT = "var httpClient = new HttpClientMock(controllerBridge);\n" +
            "var starter = new Starter(httpClient);\n" +
            "starter.doStart();\n" +
            "var pageController = starter.pageController;\n" +
            "var widgetController = starter.widgetController;\n";


}
