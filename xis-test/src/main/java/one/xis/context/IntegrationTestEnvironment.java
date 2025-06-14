package one.xis.context;

import lombok.Getter;

@Getter
class IntegrationTestEnvironment {

    private final HtmlObjects htmlObjects = new HtmlObjects();
    private final IntegrationTestScript integrationTestScript;
    private final BackendBridge backendBridge;

    IntegrationTestEnvironment(BackendBridge backendBridge) {
        this.integrationTestScript = new IntegrationTestScript(this);
        this.backendBridge = backendBridge;
    }

    void openPage(String uri) {
        integrationTestScript.getInvoker().execute(uri);
    }
}
