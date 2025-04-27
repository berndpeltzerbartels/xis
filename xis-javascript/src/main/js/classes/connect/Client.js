class Client {


    constructor() {
        this.config = undefined;
        this.clientId = randomString();
        this.userId = '';
        this.zoneId = timeZone();
        this.clientState = {}
    }

       /**
     * @public
     * @return {Promise<ClientConfig>}
     */
    loadConfig() {
        throw new Error('Not implemented');
    }

    /**
     * @public
     * @param {string} pageId
     * @return {Promise<string>}
     */
    loadPageHead(pageId) {
        throw new Error('Not implemented');
    }


    /**
     * @public
     * @param {string} pageId
     * @return {Promise<string>}
     */
    loadPageBody(pageId) {
        throw new Error('Not implemented');
    }

    /**
     * @public
     * @param {string} pageId
     * @return {Promise<any>}
     */
    loadPageBodyAttributes(pageId) {
        throw new Error('Not implemented');
    }

    /**
    * @public
    * @param {string} pageId
    * @return {Promise<string>}
    */
    loadWidget(widgetId) {
        throw new Error('Not implemented');
    }

    /**
     * @public
     * @param {ResolvedURL} resolvedURL 
     * @returns {Promise<any>}
     */
    loadPageData(resolvedURL) {
        var _this = this;
        var request = this.createPageRequest(resolvedURL, null, null);
        return this.httpConnector.post('/xis/page/model', request, {})
            .then(response => _this.deserializeResponse(response));
    }

    /**
    * @public
    * @param {WidgetInstance} widgetInstance
    * @returns {Promise<ServerReponse>}
    */
    loadWidgetData(widgetInstance, widgetState) {
        var _this = this;
        var request = this.createWidgetRequest(widgetInstance, widgetState, null, null, null);
        return this.httpConnector.post('/xis/widget/model', request)
            .then(response => _this.deserializeResponse(response));
    }

    /**
     * @public
     * @param {ResolvedURL} resolvedURL 
     * @param {String} widgetId 
     * @param {String} formBindingKey 
     * @param {any} formBindingParameters 
     */
    loadFormData(resolvedURL, widgetId, formBindingKey, formBindingParameters) {
        throw new Error('Not implemented');
    }

    /**
     * @public
     * @param {WidgetInstance} widgetInstance
     * @param {string} action
     * @param {string} actionParameters
     * @returns {Promise<ServerReponse>}
     */
    widgetLinkAction(widgetInstance, widgetState, action, actionParameters) {
        throw new Error('Not implemented');
    }

    /**
     * @public
     * @param {ResolvedURL} resolvedURL 
     * @param {string} action
     * @param {any} actionParameters
     * @returns {Promise<ServerReponse>}
     */
    pageLinkAction(resolvedURL, action, actionParameters) {
        throw new Error('Not implemented');
    }

    /**
     * @public
     * @param {ResolvedURL} resolvedURL 
     * @param {String} widgetId
     * @param {sring:string} formData
     * @param {string} action
     * @param {any} actionParameters
     * @param {string} binding
     * @returns {Promise<ServerReponse>}
     */
    formAction(resolvedURL, widgetId, formData, action, formBindigKey, formBindingParameters) {
        throw new Error('Not implemented');
    }

    /**
     * @protected
     * @param {ResolvedURL} resolvedURL 
     * @param {string} action (nullable)
     * @param {Data} formData (nullable)
     * @param {any} actionParameters (nullable)
     * @returns {ClientRequest}
     */
    createPageRequest(resolvedURL, formData, action, actionParameters) {
        var normalizedPath = resolvedURL.normalizedPath;
        var request = new ClientRequest();
        request.clientId = this.clientId;
        request.userId = this.userId;
        request.pageId = normalizedPath;
        request.action = action;
        request.formData = formData ? formData.values : {};
        request.urlParameters = resolvedURL.urlParameters;
        request.pathVariables = resolvedURL.pathVariablesAsMap();
        request.actionParameters = actionParameters;
        request.zoneId = this.zoneId;
        request.clientState = this.clientStateDataPage(normalizedPath);
        request.localStorage = this.localStorageDataPage(normalizedPath);
        return request;
    }

    /**
     * @protected
     * @param {ResolvedURL} resolvedURL 
     * @param {String} widgetId 
     * @param {string:string} formData 
     * @param {String} action 
     * @param {any} actionParameters 
     */
    createFormRequest(resolvedURL, widgetId, formData, action, formBindingKey, formBindingParameters) {
        var mappedFormData = {};
        if (formBindingKey) {
            mappedFormData[formBindingKey] = formData;
        }
        var normalizedPath = resolvedURL.normalizedPath;
        var request = new ClientRequest();
        request.clientId = this.clientId;
        request.userId = this.userId;
        request.widgetId = widgetId;
        request.pageId = normalizedPath;
        request.formBinding = formBindingKey;
        request.action = action;
        request.formData = mappedFormData;
        request.urlParameters = resolvedURL.urlParameters;
        request.pathVariables = resolvedURL.pathVariablesAsMap();
        request.bindingParameters = formBindingParameters;
        request.zoneId = this.zoneId;
        if (widgetId) { // TODO write a test
            request.clientState = this.clientStateDataWidget(widgetId);
            request.localStorage = this.localStorageDataWidget(widgetId);
        }
        if (normalizedPath) {// TODO write a test
            request.clientState = this.clientStateDataPage(normalizedPath);
            request.localStorage = this.localStorageDataPage(normalizedPath);
        }
        return request;
    }

    /**
    * @protected
    * @param {string} widgetId 
    * @param {WidgetInstance} widgetInstance
    * @param {WidgetState} widgetState
    * @param {string} action (nullable)
    * @param {Data} formData (nullable)
    * @param {string} actionParameters (nullable)
    * @returns {ClientRequest}
    */
    createWidgetRequest(widgetInstance, widgetState, action, formData, actionParameters) {
        var request = new ClientRequest();
        request.clientId = this.clientId;
        request.userId = this.userId;
        request.widgetId = widgetInstance.widget.id;
        request.action = action;
        request.formData = formData ? formData.values : {};
        request.urlParameters = widgetState.resolvedURL.urlParameters;
        request.pathVariables = widgetState.resolvedURL.pathVariablesAsMap();
        request.bindingParameters = widgetState.widgetParameters;
        request.actionParameters = actionParameters
        request.zoneId = this.zoneId;         // TODO locale ?
        request.clientState = this.clientStateDataWidget(widgetInstance.widget.id);
        request.localStorage = this.localStorageDataWidget(widgetInstance.widget.id);
        return request;
    }

    localStorageDataPage(pageId) {
       return this.localStorageData(this.config.pageAttributes[pageId]);
    }

    clientStateDataPage(pageId) {
        return this.clientStateData(this.config.pageAttributes[pageId]);
    }


    clientStateDataWidget(widgetId) {
       return this.clientStateData(this.config.widgetAttributes[widgetId]);
    }

    localStorageDataWidget(widgetId) {
        return this.localStorageData(this.config.widgetAttributes[widgetId]);
    }


    clientStateData(attributes) {
        var data = {};
        for (var key of attributes.clientStateKeys) {
            data[key] = this.clientState[key];
        }
        return data;
    }

 
    localStorageData(attributes) {
        var data = {};
        for (var key of attributes.localStorageKeys) {
            data[key] = localStorage.getItem(key);
        }
        return data;
    }



    /**
     * @protected
     * @param {string} content 
     * @returns {ClientConfig}
     */
    deserializeConfig(content) {
        var obj = JSON.parse(content);
        var config = new ClientConfig();
        config.welcomePageId = obj.welcomePageId;
        config.pageIds = obj.pageIds ? obj.pageIds : [];
        config.widgetIds = obj.widgetIds ? obj.widgetIds : [];
        config.pageAttributes = {};
        if (obj.pageAttributes) {
            for (var key of Object.keys(obj.pageAttributes)) {
                config.pageAttributes[key] = new PageAttributes(obj.pageAttributes[key]);
            }
        }
        config.widgetAttributes = {};
        if (obj.widgetAttributes) {
            for (var key of Object.keys(obj.widgetAttributes)) {
                config.widgetAttributes[key] = new WidgetAttributes(obj.widgetAttributes[key]);
            }
        }
        return config;
    }

    /**
     * Selects parameters being used in model data method's signature.
     * 
     * @protected
     * @param {Response} content 
     * @param {number} httpStatus
     * @returns {ServerReponse}
     */
    deserializeResponse(response) {
        var obj = JSON.parse(response.responseText);
        var data = obj.data ? new Data(obj.data) : new Data({});
        var formData = obj.formData ? new Data(obj.formData) : new Data({});
        var serverResponse = new ServerResponse();
        serverResponse.data = data;
        serverResponse.formData = formData;
        serverResponse.nextPageURL = obj.nextPageURL;
        serverResponse.nextWidgetId = obj.nextWidgetId;
        serverResponse.status = response.status;
        serverResponse.validatorMessages = new ValidatorMessages(obj.validatorMessages);
        serverResponse.reloadPage = obj.reloadPage;
        serverResponse.reloadWidgets = obj.reloadWidgets;
        serverResponse.localDatabaseData = obj.localDatabaseData;
        serverResponse.localStorageData = obj.localStorageData;
        serverResponse.clientScopeData = obj.clientScopeData;
        serverResponse.widgetContainerId = obj.widgetContainerId;
        data.setValue(['clientScope'], serverResponse.clientScopeData); // TODO correct ? docs ?
        return serverResponse;
    }

    storeData(response) {
      this.storeLocalStorageData(response.localStorageData);
      this.storeClientScopeData(response.clientScopeData);
      this.storeLocalDatabaseData(response.localDatabaseData);
    }

    storeLocalStorageData(localStorageData) {
        for (var key of Object.keys(localStorageData)) {
            localStorage.setItem(key, localStorageData[key]);
        }
    }

    storeClientScopeData(clientScopeData) {
        for (var key of Object.keys(clientScopeData)) {
            this.clientState[key] = clientScopeData[key];
        }
    }
    storeLocalDatabaseData(localDatabaseData) {
        // TODO create and configure db
        for (var key of Object.keys(localDatabaseData)) {
            this.localDatabase.setItem(key, localDatabaseData[key]);
        }
    }



}