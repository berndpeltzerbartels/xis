package one.xis.context;

import lombok.Getter;
import one.xis.js.Javascript;
import one.xis.test.dom.Node;
import one.xis.test.js.Array;
import one.xis.test.js.JSUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

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
        return Javascript.getScript(CLASSES, FUNCTIONS, TEST, TEST_MAIN);
    }

    JavascriptFunction getInvoker() {
        return getIntegrationTestFunctions().getInvoker();
    }

    void reset() {
        var resetFunction = getIntegrationTestFunctions().getReset();
        updateBindings(resetFunction);
        resetFunction.execute();
    }


    private IntegrationTestFunctions createIntegrationTestFunctions() {
        var context = JSUtil.context(createBindings());
        var value = JSUtil.execute(script, context);
        var invoker = new JavascriptFunctionContext(value.getArrayElement(0), context);
        var reset = new JavascriptFunctionContext(value.getArrayElement(1), context);
        return new IntegrationTestFunctions(invoker, reset);

    }

    private Map<String, Object> createBindings() {
        var bindings = new HashMap<String, Object>();
        bindings.put("backendBridge", testEnvironment.getBackendBridge());
        bindings.put("localStorage", testEnvironment.getHtmlObjects().getLocalStorage());
        bindings.put("sessionStorage", testEnvironment.getHtmlObjects().getSessionStorage());
        bindings.put("document", testEnvironment.getHtmlObjects().getDocument());
        bindings.put("window", testEnvironment.getHtmlObjects().getWindow());
        bindings.put("htmlToElement", testEnvironment.getHtmlObjects().getHtmlToElement());
        bindings.put("atob", testEnvironment.getHtmlObjects().getAtob());
        bindings.put("encodeURIComponent", testEnvironment.getHtmlObjects().getEncodeURIComponent());
        bindings.put("Node", Node.class);
        bindings.put("Array", new Array());
        bindings.put("debug", debugFunction);
        // bindings.put("console", testEnvironment.getHTML_OBJECTS().getConsole());
        return bindings;
    }

    private void updateBindings(JavascriptFunction invoker) {
        invoker.setBinding("backendBridge", testEnvironment.getBackendBridge());
        invoker.setBinding("localStorage", testEnvironment.getHtmlObjects().getLocalStorage());
        invoker.setBinding("sessionStorage", testEnvironment.getHtmlObjects().getSessionStorage());
        invoker.setBinding("document", testEnvironment.getHtmlObjects().getDocument());
        invoker.setBinding("window", testEnvironment.getHtmlObjects().getWindow());
        invoker.setBinding("htmlToElement", testEnvironment.getHtmlObjects().getHtmlToElement());
        invoker.setBinding("atob", testEnvironment.getHtmlObjects().getAtob());
        invoker.setBinding("encodeURIComponent", testEnvironment.getHtmlObjects().getEncodeURIComponent());
        invoker.setBinding("Node", Node.class);
        invoker.setBinding("Array", new Array());
        invoker.setBinding("debug", debugFunction);
        //  invoker.setBinding("console", testEnvironment.getHTML_OBJECTS().getConsole());
    }

    private final BiConsumer<String, Object> debugFunction = this::debug;

    public void debug(String text, Object args) {
        System.out.printf("DEBUG: " + text + "\n", args);
    }
}
