package one.xis.context;

import lombok.Getter;
import one.xis.js.Javascript;
import one.xis.test.dom.NodeConstants;
import one.xis.test.js.JSUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static one.xis.js.JavascriptSource.*;

@Getter
class IntegrationTestScript {

    private final IntegrationTestEnvironment testEnvironment;
    private final String script;
    private final IntegrationTestFunctions integrationTestFunctions;
    private final List<TestScriptExtension> extensions;

    IntegrationTestScript(IntegrationTestEnvironment testEnvironment) {
        this.testEnvironment = testEnvironment;
        this.extensions = ServiceLoader.load(TestScriptExtension.class).stream()
                .map(ServiceLoader.Provider::get)
                .toList();
        this.script = testScript();
        this.integrationTestFunctions = createIntegrationTestFunctions();
    }

    private String testScript() {
        var base = Javascript.getScript(EVENT_REGISTRY, CLASSES, FUNCTIONS, TEST);
        var extensionScripts = new StringBuilder();
        for (var ext : extensions) {
            extensionScripts.append(ext.getAdditionalScript()).append("\n");
        }
        return base + extensionScripts + Javascript.getScript(TEST_MAIN);
    }

    JavascriptFunction getInvoker() {
        return getIntegrationTestFunctions().getInvoker();
    }

    void reset() {
        var resetFunction = getIntegrationTestFunctions().getReset();
        updateBindings(resetFunction, createBindings());
        resetFunction.execute();
    }

    private IntegrationTestFunctions createIntegrationTestFunctions() {
        var context = JSUtil.context(createBindings());
        var value = JSUtil.execute(script, context);
        var invoker = new JavascriptFunctionContext(value.getArrayElement(0), context);
        var reset = new JavascriptFunctionContext(value.getArrayElement(1), context);
        var simulatePushEvent = new JavascriptFunctionContext(value.getArrayElement(2), context);
        // Give extensions direct access to the simulatePushEvent JS-function
        extensions.forEach(ext -> ext.onScriptReady(simulatePushEvent));
        return new IntegrationTestFunctions(invoker, reset, simulatePushEvent);
    }

    public Map<String, Object> createBindings() {
        var bindings = new HashMap<String, Object>();
        bindings.put("backendBridge", testEnvironment.getBackendBridge());
        bindings.put("localStorage", testEnvironment.getHtmlObjects().getLocalStorage());
        bindings.put("sessionStorage", testEnvironment.getHtmlObjects().getSessionStorage());
        bindings.put("document", testEnvironment.getHtmlObjects().getDocument());
        bindings.put("window", testEnvironment.getHtmlObjects().getWindow());
        bindings.put("htmlToElement", testEnvironment.getHtmlObjects().getHtmlToElement());
        bindings.put("atob", testEnvironment.getHtmlObjects().getAtob());
        bindings.put("encodeURIComponent", testEnvironment.getHtmlObjects().getEncodeURIComponent());
        bindings.put("setTimeout", testEnvironment.getHtmlObjects().getSetTimeout());
        bindings.put("Node", new NodeConstants());
        bindings.putAll(BrowserFunctions.BINDINGS);
        bindings.put("console", testEnvironment.getHtmlObjects().getConsole());
        for (var ext : extensions) {
            bindings.putAll(ext.getAdditionalBindings(testEnvironment));
        }
        return bindings;
    }

    private void updateBindings(JavascriptFunction invoker, Map<String, Object> bindings) {
        bindings.forEach(invoker::setBinding);
    }

    private final BiConsumer<String, Object> debugFunction = this::debug;
    private final Function<Object, Boolean> isFloatFunction = this::isFloat;
    private final Function<Object, Boolean> isIntFunction = this::isInt;
    private final Function<Object, Object> parseFloatFunction = this::parseFloat;
    private final Function<Object, Object> parseIntFunction = this::parseInt;
    private final Function<Object, Boolean> isNaNFunction = this::isNaN;

    public void debug(String text, Object args) {
        System.out.printf("DEBUG: " + text + "\n", args);
    }

    public boolean isFloat(Object obj) {
        if (obj == null) return false;
        if (obj instanceof Number n) {
            double v = n.doubleValue();
            return !Double.isNaN(v) && !Double.isInfinite(v);
        }
        return false;
    }

    public boolean isInt(Object obj) {
        if (obj == null) return false;
        if (obj instanceof Number n) {
            double v = n.doubleValue();
            return !Double.isNaN(v) && !Double.isInfinite(v) && v == Math.floor(v);
        }
        return false;
    }

    public Object parseFloat(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Number n) return n.doubleValue();
        try {
            return Double.parseDouble(obj.toString());
        } catch (NumberFormatException e) {
            return NaN;
        }
    }

    public Object parseInt(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(obj.toString());
        } catch (NumberFormatException e) {
            return NaN;
        }
    }

    public boolean isNaN(Object obj) {
        return obj == NaN;
    }

    static final Object NaN = new Object();
}
