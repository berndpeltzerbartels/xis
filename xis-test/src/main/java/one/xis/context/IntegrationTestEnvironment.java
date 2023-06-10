package one.xis.context;

import lombok.Getter;
import one.xis.resource.Resource;
import one.xis.test.dom.HtmlObjects;

@Getter
class IntegrationTestEnvironment {

    private final HtmlObjects htmlObjects;
    private final IntegrationTestScript integrationTestScript;
    private final BackendBridgeProvider backendBridgeProvider;
    private GraalVMFunction starter;

    IntegrationTestEnvironment(Resource scriptResource, Resource rootPage, String initialUri) {
        this.htmlObjects = new HtmlObjects(rootPage, initialUri);
        this.backendBridgeProvider = new BackendBridgeProvider();
        this.integrationTestScript = new IntegrationTestScript(scriptResource, this);
    }

    void openPage(String uri, AppContext appContext) {
        backendBridgeProvider.setBackendBridge(appContext.getSingleton(BackendBridge.class));
        if (starter == null) {
            starter = integrationTestScript.runScript();
        } else {
            this.starter.execute(uri);
        }
    }

    void reset() {
        this.htmlObjects.reset();
    }

    void afterPageLoaded() {
        htmlObjects.finalizeDocument();
    }

}
