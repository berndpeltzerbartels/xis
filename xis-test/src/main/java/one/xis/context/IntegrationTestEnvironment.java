package one.xis.context;

import lombok.Getter;
import one.xis.test.dom.HtmlObjects;

@Getter
class IntegrationTestEnvironment {

    private final HtmlObjects htmlObjects;
    private final IntegrationTestScript integrationTestScript;
    private final BackendBridge backendBridge;

    IntegrationTestEnvironment(BackendBridge backendBridge) {
        this.htmlObjects = new HtmlObjects();
        this.integrationTestScript = new IntegrationTestScript(this);
        this.backendBridge = backendBridge;
    }

    void openPage(String uri) {
        integrationTestScript.getInvoker().execute(uri);
    }
}
