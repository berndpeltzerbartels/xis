class BackendService {

    constructor(client, pageController) {
        this.client = client;
        this.pageController = pageController;
        this.config = undefined;
    }
    
     /**
     * @public
     * @param {ClientConfig} config
     * @returns {Promise<ClientConfig>}
     */
     setConfig(config) {
        var _this = this;
        return new Promise((resolve, _) => {
            _this.config = config;
            resolve(config);
        });
    }

    /**
     * 
     * @param {WidgetInstance} widgetInstance
     * @param {WidgetState} widgetState
     * @param {WidgetContainerHandler| FormHandler} invokingHandler
     * @returns Promise<Data>
     */
    loadWidgetData(widgetInstance, widgetState, invokingHandler) {
        var resolvedURL = widgetState.resolvedURL;
        return this.client.loadWidgetData(widgetInstance, widgetState)
            .then(response => this.triggerAdditionalReloadsOnDemand(response)) 
            .then(response => this.triggerReactiveStateUpdates(response, invokingHandler))
            .then(response => response.data)
            .then(data => { data.setValue(['urlParameters'], resolvedURL.urlParameters); return data; })
            .then(data => { data.setValue(['pathVariables'], resolvedURL.pathVariablesAsMap()); return data; })
            .then(data => { data.setValue(['widgetParameters'], widgetState.widgetParameters); return data; })
    }

    /**
     * Triggers reactive state updates when widget data contains client state changes.
     * This ensures that other parts of the page (like title, other widgets) that depend 
     * on the same state variables get refreshed.
     * 
     * @param {ServerResponse} response - Server response from widget data loading
     * @returns {ServerResponse} - The same response for chaining
     */
    triggerReactiveStateUpdates(response, invokingHandler) {
        // Check if response contains any reactive state updates
        if (this.hasStateVariables(response)) {
            this.pageController.storeRefresh(invokingHandler);
        }
        return response;
    }

    /**
     * Loads form data and handles reactive state updates.
     * Similar to widget data loading but for forms.
     * 
     * @param {ResolvedURL} resolvedURL - The current page URL
     * @param {string} widgetId - Widget ID if form is in a widget
     * @param {string} formBindingKey - The form binding key
     * @param {any} formBindingParameters - Form binding parameters
     * @param {FormHandler} invokingHandler - The form handler that initiated the load
     * @returns {Promise<ServerResponse>} - Promise resolving to server response
     */
    loadFormData(resolvedURL, widgetId, formBindingKey, formBindingParameters, invokingHandler) {
        return this.client.loadFormData(resolvedURL, widgetId, formBindingKey, formBindingParameters)
            .then(response => this.triggerAdditionalReloadsOnDemand(response))
            .then(response => this.triggerReactiveStateUpdates(response, invokingHandler))
    }

    /**
     * Checks if the server response contains any reactive state variables that require
     * the stateRefresh mechanism to update other parts of the page.
     * 
     * @param {ServerResponse} serverResponse - The server response to check
     * @returns {boolean} - True if reactive state updates are needed
     */
    hasStateVariables(serverResponse) {
        // Check for client state data
        if (serverResponse.clientStateData && Object.keys(serverResponse.clientStateData).length > 0) {
            return true;
        }
        
        // Check for local storage data  
        if (serverResponse.localStorageData && Object.keys(serverResponse.localStorageData).length > 0) {
            return true;
        }
        
        // Check for local database data
        if (serverResponse.localDatabaseData && Object.keys(serverResponse.localDatabaseData).length > 0) {
            return true;
        }
        
        return false;
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