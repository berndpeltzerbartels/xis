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
    private static JavascriptFunction invoker;

    IntegrationTestScript(IntegrationTestEnvironment testEnvironment) {
        this.testEnvironment = testEnvironment;
        this.script = testScript();
    }

    private String testScript() {
        return Javascript.getScript(CLASSES, FUNCTIONS, TEST, TEST_MAIN);
    }

    JavascriptFunction getInvoker() {
        if (invoker == null) {
            invoker = createInvoker();
        } else {
            updateBindings();
        }
        return invoker;
    }

    private JavascriptFunction createInvoker() {
        return JSUtil.function(script, createBindings());
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

    private void updateBindings() {
        invoker.setBinding("backendBridge", testEnvironment.getBackendBridge());
        invoker.setBinding("localStorage", testEnvironment.getHtmlObjects().getLocalStorage());
        invoker.setBinding("document", testEnvironment.getHtmlObjects().getRootPage());
        invoker.setBinding("window", testEnvironment.getHtmlObjects().getWindow());
        invoker.setBinding("htmlToElement", testEnvironment.getHtmlObjects().getHtmlToElement());

    }
}
