class ReloadTrigger {

    triggerPageReloadOnDemand(serverResponse) {
        if (serverResponse.reloadPage) {
            app.pageController.triggerPageReload();
         }
         return serverResponse;
    }

    triggerWidgetReloadsOnDemand(serverResponse) {
        for (var widgetId in serverResponse.reloadWidgets) {
            var containerHandler = app.widgetContainers.findContainerHandlerByWidgetId(widgetId);
            if (containerHandler) {
                containerHandler.triggerWidgetReload();
            }
         }
         return serverResponse;
    }
}