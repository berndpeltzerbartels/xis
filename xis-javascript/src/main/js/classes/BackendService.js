class BackendService {

    constructor(client) {
        this.client = client;
    }

    /**
     * 
     * @param {WidgetInstance} widgetInstance 
     * @param {WidgetState} widgetState 
     * @returns Promise<Data>
     */
    loadWidgetData(widgetInstance, widgetState) {
        var resolvedURL = widgetState.resolvedURL;
        var _this = this;
        return this.client.loadWidgetData(widgetInstance, widgetState)
            .then(response => _this.triggerAdditionalReloadsOnDemand(response)) 
            .then(response => response.data)
            .then(data => { data.setValue(['urlParameters'], resolvedURL.urlParameters); return data; })
            .then(data => { data.setValue(['pathVariables'], resolvedURL.pathVariablesAsMap()); return data; })
            .then(data => { data.setValue(['widgetParameters'], widgetState.widgetParameters); return data; })
    }

    triggerAdditionalReloadsOnDemand(response) {
      this.triggerWidgetReloadsOnDemand(response);
      this.triggerPageReloadOnDemand(response);
        return response;
    }

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