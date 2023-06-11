package one.xis.context;

import lombok.Getter;
import one.xis.js.Javascript;
import one.xis.test.js.JSUtil;

import javax.script.ScriptException;
import java.util.HashMap;
import java.util.Map;

import static one.xis.js.JavascriptSource.*;


@Getter
class IntegrationTestScript {

    private final IntegrationTestEnvironment testSingletons;
    private final String script;

    IntegrationTestScript(IntegrationTestEnvironment testSingletons) {
        this.testSingletons = testSingletons;
        this.script = testScript();
    }

    private String testScript() {
        return Javascript.getScript(CLASSES, FUNCTIONS, TEST, TEST_MAIN);
    }

    GraalVMFunction runScript() {
        try {
            return new GraalVMFunction(JSUtil.execute(script, createBindings()));
        } catch (ScriptException e) {
            throw new RuntimeException("Script failed :" + e.getMessage() + " at line " + e.getLineNumber() + ", column " + e.getColumnNumber(), e);
        }
    }

    private Map<String, Object> createBindings() {
        var bindings = new HashMap<String, Object>();
        bindings.put("backendBridgeProvider", testSingletons.getBackendBridgeProvider());
        bindings.put("localStorage", testSingletons.getHtmlObjects().getLocalStorage());
        bindings.put("document", testSingletons.getHtmlObjects().getRootPage());
        bindings.put("window", testSingletons.getHtmlObjects().getWindow());
        bindings.put("htmlToElement", testSingletons.getHtmlObjects().getHtmlToElement());
        return bindings;
    }
}
