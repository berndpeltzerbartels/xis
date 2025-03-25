class ReloadTrigger {
    constructor(client) {
        this.client = client;
    }
    
    triggerReloads(serverRepsonse) {
        if (serverRepsonse.reloadPage) {
           app.pageController.triggerPageReload();
        }
        for (var widgetId in serverRepsonse.reloadWidgets) {
           
        }
    }
}