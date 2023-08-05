package one.xis.context;

import lombok.Getter;

@Getter
class IntegrationTestEnvironment {

    private static final HtmlObjects HTML_OBJECTS = new HtmlObjects();
    private final IntegrationTestScript integrationTestScript;
    private final BackendBridge backendBridge;

    IntegrationTestEnvironment(BackendBridge backendBridge) {
        this.integrationTestScript = new IntegrationTestScript(this);
        this.backendBridge = backendBridge;
    }

    HtmlObjects getHTML_OBJECTS() {
        return HTML_OBJECTS;
    }

    void openPage(String uri) {
        integrationTestScript.getInvoker().execute(uri);
    }
}
