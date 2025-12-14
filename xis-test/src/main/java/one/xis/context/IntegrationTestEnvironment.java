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
        updateLocationProperties(uri);
        integrationTestScript.getInvoker().execute(uri);
    }

    private void updateLocationProperties(String uri) {
        var location = htmlObjects.getWindow().getLocation();
        
        // Parse URI in pathname and search
        String pathname = uri;
        String search = "";
        
        int queryStart = uri.indexOf('?');
        if (queryStart != -1) {
            pathname = uri.substring(0, queryStart);
            search = uri.substring(queryStart);
        }
        
        // Set location properties
        location.setPathname(pathname);
        location.setHref("http://testserver" + uri);
        location.setOrigin("http://testserver");
        location.setSearch(search);
    }
}
