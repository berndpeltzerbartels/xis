package one.xis.context;

import lombok.Getter;
import one.xis.js.Javascript;
import one.xis.test.dom.NodeConstants;
import one.xis.test.js.JSUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static one.xis.js.JavascriptSource.*;


@Getter
class IntegrationTestScript {

    private final IntegrationTestEnvironment testEnvironment;
    private final String script;
    private final IntegrationTestFunctions integrationTestFunctions;

    IntegrationTestScript(IntegrationTestEnvironment testEnvironment) {
        this.testEnvironment = testEnvironment;
        this.script = testScript();
        this.integrationTestFunctions = createIntegrationTestFunctions();
    }

    private String testScript() {
        return Javascript.getScript(EVENT_REGISTRY, CLASSES, FUNCTIONS, TEST, TEST_MAIN);
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
        return new IntegrationTestFunctions(invoker, reset);

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
        if (obj == null) {
            return false;
        }
        if (obj instanceof Number) {
            double value = ((Number) obj).doubleValue();
            return !Double.isNaN(value) && !Double.isInfinite(value);
        }
        return false;
    }

    public boolean isInt(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof Number) {
            double value = ((Number) obj).doubleValue();
            return !Double.isNaN(value) && !Double.isInfinite(value) && value == Math.floor(value);
        }
        return false;
    }

    public Object parseFloat(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Number) {
            return ((Number) obj).doubleValue();
        }
        try {
            return Double.parseDouble(obj.toString());
        } catch (NumberFormatException e) {
            return NaN;
        }
    }

    public Object parseInt(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        }
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
