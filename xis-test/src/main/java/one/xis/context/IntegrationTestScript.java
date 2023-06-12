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

    @SuppressWarnings("unchecekd")
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
        bindings.put("document", testEnvironment.getHtmlObjects().getRootPage());
        bindings.put("window", testEnvironment.getHtmlObjects().getWindow());
        bindings.put("htmlToElement", testEnvironment.getHtmlObjects().getHtmlToElement());
        return bindings;
    }

    private void updateBindings(JavascriptFunction invoker) {
        invoker.setBinding("backendBridge", testEnvironment.getBackendBridge());
        invoker.setBinding("localStorage", testEnvironment.getHtmlObjects().getLocalStorage());
        invoker.setBinding("document", testEnvironment.getHtmlObjects().getRootPage());
        invoker.setBinding("window", testEnvironment.getHtmlObjects().getWindow());
        invoker.setBinding("htmlToElement", testEnvironment.getHtmlObjects().getHtmlToElement());

    }
}
