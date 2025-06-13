package one.xis.context;

import lombok.Getter;
import one.xis.js.Javascript;
import one.xis.test.js.JSUtil;

import java.util.HashMap;
import java.util.Map;

import static one.xis.js.JavascriptSource.*;


@Getter
class IntegrationTestScript {

    private final IntegrationTestEnvironment testEnvironment;
    private final String script;
    private static IntegrationTestFunctions integrationTestFunctions;

    IntegrationTestScript(IntegrationTestEnvironment testEnvironment) {
        this.testEnvironment = testEnvironment;
        this.script = testScript();
    }

    private String testScript() {
        return Javascript.getScript(CLASSES, FUNCTIONS, TEST, TEST_MAIN);
    }

    JavascriptFunction getInvoker() {
        return getIntegrationTestFunctions().getInvoker();
    }

    private IntegrationTestFunctions getIntegrationTestFunctions() {
        if (integrationTestFunctions == null) {
            integrationTestFunctions = createIntegrationTestFunctions();
        } else {
            integrationTestFunctions.getReset().execute();
            updateBindings(integrationTestFunctions.getInvoker());
        }
        return integrationTestFunctions;
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
        bindings.put("localStorage", testEnvironment.getHTML_OBJECTS().getLocalStorage());
        bindings.put("sessionStorage", testEnvironment.getHTML_OBJECTS().getSessionStorage());
        bindings.put("document", testEnvironment.getHTML_OBJECTS().getDocument());
        bindings.put("window", testEnvironment.getHTML_OBJECTS().getWindow());
        bindings.put("htmlToElement", testEnvironment.getHTML_OBJECTS().getHtmlToElement());
        bindings.put("atob", testEnvironment.getHTML_OBJECTS().getAtob());
        // bindings.put("console", testEnvironment.getHTML_OBJECTS().getConsole());
        return bindings;
    }

    private void updateBindings(JavascriptFunction invoker) {
        invoker.setBinding("backendBridge", testEnvironment.getBackendBridge());
        invoker.setBinding("localStorage", testEnvironment.getHTML_OBJECTS().getLocalStorage());
        invoker.setBinding("sessionStorage", testEnvironment.getHTML_OBJECTS().getSessionStorage());
        invoker.setBinding("document", testEnvironment.getHTML_OBJECTS().getDocument());
        invoker.setBinding("window", testEnvironment.getHTML_OBJECTS().getWindow());
        invoker.setBinding("htmlToElement", testEnvironment.getHTML_OBJECTS().getHtmlToElement());
        invoker.setBinding("atob", testEnvironment.getHTML_OBJECTS().getAtob());
        //  invoker.setBinding("console", testEnvironment.getHTML_OBJECTS().getConsole());
    }
}
