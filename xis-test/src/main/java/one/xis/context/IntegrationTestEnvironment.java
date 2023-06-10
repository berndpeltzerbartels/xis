package one.xis.context;

import lombok.Getter;
import one.xis.resource.Resource;
import one.xis.test.dom.HtmlObjects;

@Getter
class IntegrationTestEnvironment {

    private final HtmlObjects htmlObjects;
    private final IntegrationTestScript integrationTestScript;
    private final BackendBridgeProvider backendBridgeProvider;
    //private GraalVMFunction starter;

    IntegrationTestEnvironment(Resource scriptResource, Resource rootPage) {
        this.htmlObjects = new HtmlObjects(rootPage);
        this.backendBridgeProvider = new BackendBridgeProvider();
        this.integrationTestScript = new IntegrationTestScript(scriptResource, this);
    }

    void openPage(String uri, AppContext appContext) {
        htmlObjects.getRootPage().location.pathname = uri;
        backendBridgeProvider.setBackendBridge(appContext.getSingleton(BackendBridge.class));
        integrationTestScript.runScript();
        htmlObjects.finalizeDocument();
    }

    void reset() {
        this.htmlObjects.reset();
    }

    void afterPageLoaded() {
        htmlObjects.finalizeDocument();
    }

}
