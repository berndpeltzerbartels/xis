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
     * @returns Promise<Data>
     */
    loadWidgetData(widgetInstance, widgetState) {
        var resolvedURL = widgetState.resolvedURL;
        return this.client.loadWidgetData(widgetInstance, widgetState)
            .then(response => this.triggerAdditionalReloadsOnDemand(response))
            .then(response => this.triggerReactiveStateUpdates(response))
            .then(response => response.data)
            .then(data => { data.setValue(['urlParameters'], resolvedURL.urlParameters); return data; })
            .then(data => { data.setValue(['pathVariables'], resolvedURL.pathVariablesAsMap()); return data; })
            .then(data => { data.setValue(['widgetParameters'], widgetState.widgetParameters); return data; })
    }

        /**
     * Triggers reactive state updates after receiving server response
     * @param {ServerResponse} response
     * @returns {ServerResponse} - The same response for chaining
     */
    triggerReactiveStateUpdates(response) {
        // Check for any reactive data changes
        debugger;
        const hasReactiveChanges = (response.globalVariableData && Object.keys(response.globalVariableData).length > 0) ||
                                  (response.clientStateData && Object.keys(response.clientStateData).length > 0) ||
                                  (response.localStorageData && Object.keys(response.localStorageData).length > 0) ||
                                  (response.localDatabaseData && Object.keys(response.localDatabaseData).length > 0);
        
        if (hasReactiveChanges) {
            app.eventPublisher.publish(EventType.REACTIVE_DATA_CHANGED, response);
            // If a RenderCoordinator is available and globals are present, run the coordinated update
            if (app.renderCoordinator && response.globalVariableData && Object.keys(response.globalVariableData).length > 0) {
                try {
                    app.renderCoordinator.applyGlobalUpdate(response.globalVariableData, null);
                } catch (e) {
                    // swallow to avoid breaking reactive flow
                    console.error('RenderCoordinator.applyGlobalUpdate failed', e);
                }
            }
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