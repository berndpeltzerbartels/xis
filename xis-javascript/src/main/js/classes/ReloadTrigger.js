class ReloadTrigger {
    constructor(client) {
        this.client = client;
    }
    
    triggerReloads(serverResponse) {
        if (serverResponse.reloadPage) {
           app.pageController.triggerPageReload();
        }
        for (var widgetId in serverResponse.reloadWidgets) {
           var containerHandler = app.widgetContainers.findContainerHandlerByWidgetId(widgetId);
           if (containerHandler) {
               containerHandler.triggerWidgetReload();
           }
        }
    }
}