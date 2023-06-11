package one.xis.context;

import lombok.Getter;
import one.xis.test.dom.HtmlObjects;

@Getter
class IntegrationTestEnvironment {

    private final HtmlObjects htmlObjects;
    private final IntegrationTestScript integrationTestScript;
    private final BackendBridgeProvider backendBridgeProvider;
    private GraalVMFunction openPage;

    IntegrationTestEnvironment() {
        this.htmlObjects = new HtmlObjects();
        this.backendBridgeProvider = new BackendBridgeProvider();
        this.integrationTestScript = new IntegrationTestScript(this);
        this.openPage = integrationTestScript.runScript();
    }

    void openPage(String uri, AppContext appContext) {
        backendBridgeProvider.setBackendBridge(appContext.getSingleton(BackendBridge.class));
        openPage.execute(uri);
        htmlObjects.finalizeDocument();
    }

    void reset() {
        this.htmlObjects.reset();
    }

    void afterPageLoaded() {
        htmlObjects.finalizeDocument();
    }

}
