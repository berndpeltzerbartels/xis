package one.xis.context;

import lombok.Getter;
import one.xis.resource.Resource;
import one.xis.test.js.JSUtil;

import javax.script.ScriptException;
import java.util.HashMap;
import java.util.Map;


@Getter
class IntegrationTestScript {

    private final IntegrationTestEnvironment testSingletons;
    private final String script;

    IntegrationTestScript(Resource scriptResource, IntegrationTestEnvironment testSingletons) {
        this.testSingletons = testSingletons;
        this.script = scriptResource.getContent() + "\n" + START_SCRIPT;
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

    private static final String START_SCRIPT = "var httpClient = new HttpClientMock(backendBridgeProvider);\n" +
            "var starter = new Starter(httpClient);\n" +
            "starter.doStart();\n" +
            "var pageController = starter.pageController;\n" +
            "var widgetController = starter.widgetController;\n" +
            "var initializer = starter.initializer;\n" +
            "openForTesting";

}
