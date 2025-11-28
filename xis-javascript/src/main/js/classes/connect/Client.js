
// Annahme: TagContentSetter ist global verfügbar (z.B. window.TagContentSetter)
class Client {


    /** * @constructor
     * Initializes a new instance of the Client class.
     * This class serves as a base for HTTP clients that interact with the server.
     * It provides methods to load configuration, page data, widget data, and handle server responses
     */
    constructor() {
        this.config = undefined;
        this.clientId = randomString();
        this.zoneId = timeZone();
        this.tagContentSetter = new TagContentSetter();
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
        throw new Error('Not implemented');
    }

    /**
    * @public
    * @param {WidgetInstance} widgetInstance
    * @returns {Promise<ServerReponse>}
    */
    loadWidgetData(widgetInstance, widgetState) {
        throw new Error('Not implemented');
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
     * @public
     * @param {string} renewToken
     * @returns {Promise<TokenResponse>}
     */
    sendRenewTokenRequest(renewToken) {
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
        request.pageId = normalizedPath;
        request.pageUrl = resolvedURL.url;
        request.action = action;
        request.formData = formData ? formData.values : {};
        request.urlParameters = resolvedURL.urlParameters;
        request.pathVariables = resolvedURL.pathVariablesAsMap();
        request.actionParameters = actionParameters ? actionParameters : {};
        request.zoneId = this.zoneId;
        request.sessionStorageData = this.sessionStorageDataPage(normalizedPath);
        request.localStorageData = this.localStorageDataPage(normalizedPath);
        request.clientStorageData = this.clientStorageDataPage(normalizedPath);
        request.globalVariableData = this.globalVariableDataPage(normalizedPath);
        request.localDatabaseData = {};
        request.type = 'page';
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
        request.widgetId = widgetId;
        request.pageId = normalizedPath;
        request.pageUrl = resolvedURL.url;
        request.formBinding = formBindingKey;
        request.action = action;
        request.formData = mappedFormData;
        request.urlParameters = resolvedURL.urlParameters;
        request.pathVariables = resolvedURL.pathVariablesAsMap();
        request.bindingParameters = formBindingParameters;
        request.zoneId = this.zoneId;
        request.type = request.widgetId ? 'widget' : 'page';
        if (widgetId) { // TODO write a test
            request.sessionStorageData = this.sessionStorageDataWidget(widgetId);
            request.localStorageData = this.localStorageDataWidget(widgetId);
            request.clientStorageData = this.clientStorageDataWidget(widgetId);
            request.globalVariableData = this.globalVariableDataWidget(widgetId);
        }
        if (normalizedPath) {// TODO write a test
            request.sessionStorageData = this.sessionStorageDataPage(normalizedPath);
            request.localStorageData = this.localStorageDataPage(normalizedPath);
            request.clientStorageData = this.clientStorageDataPage(normalizedPath);
            request.globalVariableData = this.globalVariableDataPage(normalizedPath);
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
        request.widgetId = widgetInstance.widget.id;
        request.action = action;
        request.formData = formData ? formData.values : {};
        request.urlParameters = widgetState.resolvedURL.urlParameters;
        request.pathVariables = widgetState.resolvedURL.pathVariablesAsMap();
        request.bindingParameters = widgetState.widgetParameters;
        request.actionParameters = actionParameters ? actionParameters : {};
        request.zoneId = this.zoneId;         // TODO locale ?
        request.sessionStorageData = this.sessionStorageDataWidget(widgetInstance.widget.id);
        request.localStorageData = this.localStorageDataWidget(widgetInstance.widget.id);
        request.clientStorageData = this.clientStorageDataWidget(widgetInstance.widget.id);
        request.globalVariableData = this.globalVariableDataWidget(widgetInstance.widget.id);
        request.localDatabaseData = {};
        request.type = 'widget';
        return request;
    }

    localStorageDataPage(pageId) {
        return this.localStorageData(this.config.pageAttributes[pageId]);
    }

    sessionStorageDataPage(pageId) {
        return this.sessionStorageData(this.config.pageAttributes[pageId]);
    }

    sessionStorageDataWidget(widgetId) {
        return this.sessionStorageData(this.config.widgetAttributes[widgetId]);
    }

    localStorageDataWidget(widgetId) {
        return this.localStorageData(this.config.widgetAttributes[widgetId]);
    }

    clientStorageDataPage(pageId) {
        return this.clientStorageData(this.config.pageAttributes[pageId]);
    }

    clientStorageDataWidget(widgetId) {
        return this.clientStorageData(this.config.widgetAttributes[widgetId]);
    }

    globalVariableDataPage(pageId) {
        return this.globalVariableData(this.config.pageAttributes[pageId]);
    }

    globalVariableDataWidget(widgetId) {
        return this.globalVariableData(this.config.widgetAttributes[widgetId]);
    }


    sessionStorageData(attributes) {
        var data = {};
        for (var key of attributes.sessionStorageKeys) {
            data[key] = app.sessionStorage.getValue(key);
        }
        return data;
    }


    localStorageData(attributes) {
        var data = {};
        for (var key of attributes.localStorageKeys) {
            data[key] = app.localStorage.getValue(key);
        }
        return data;
    }

    clientStorageData(attributes) {
        var data = {};
        for (var key of attributes.clientStorageKeys) {
            data[key] = app.clientStorage.getValue(key);
        }
        return data;
    }

    globalVariableData(attributes) {
        var data = {};
        for (var key of attributes.globalVariableKeys) {
            data[key] = app.globals.getValue(key);
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
        serverResponse.nextURL = obj.nextURL;
        serverResponse.nextWidgetId = obj.nextWidgetId;
        serverResponse.status = response.status;
        serverResponse.validatorMessages = new ValidatorMessages(obj.validatorMessages);
        serverResponse.reloadPage = obj.reloadPage;
        serverResponse.reloadWidgets = obj.reloadWidgets;
        serverResponse.localDatabaseData = obj.localDatabaseData;
        serverResponse.localStorageData = obj.localStorageData;
        serverResponse.clientStorageData = obj.clientStorageData;
        serverResponse.globalVariableData = obj.globalVariableData;
        serverResponse.sessionStorageData = obj.sessionStorageData;
        serverResponse.widgetContainerId = obj.widgetContainerId;
        serverResponse.redirectUrl = obj.redirectUrl;
        serverResponse.tagVariables = obj.tagVariables || {};
        serverResponse.idVariables = obj.idVariables || {};
        serverResponse.actionProcessing = obj.actionProcessing || 'NONE';
        serverResponse.updateEventKeys = obj.updateEventKeys || [];
        data.setValue(['sessionStorage'], serverResponse.sessionStorageData);
        data.setValue(['localStorage'], serverResponse.localStorageData);
        data.setValue(['clientStorage'], serverResponse.clientStorageData);
        data.setValue(['global'], serverResponse.globalVariableData);
        data.setValue(['validation'], obj.validatorMessages);
        this.storeData(serverResponse);
        this.tagContentSetter.apply(document, serverResponse.idVariables, serverResponse.tagVariables);
        return serverResponse;
    }

    storeData(response) {
        this.storeLocalStorageData(response.localStorageData);
        this.storeSessionStorageData(response.sessionStorageData);
        this.storeClientStorageData(response.clientStorageData);
        this.storeGlobalVariableData(response.globalVariableData);
        this.storeLocalDatabaseData(response.localDatabaseData);
    }

    storeLocalStorageData(localStorageData) {
        if (!localStorageData) {
            return;
        }
        app.localStorage.saveData(localStorageData);
    }

    storeSessionStorageData(sessionStorageData) {
        app.sessionStorage.saveData(sessionStorageData);
    }

    storeClientStorageData(clientStorageData) {
        if (!clientStorageData) {
            return;
        }
        app.clientStorage.saveData(clientStorageData);
    }

    storeGlobalVariableData(globalVariableData) {
        if (!globalVariableData) {
            return;
        }
        app.globals.saveData(globalVariableData);
    }

    storeLocalDatabaseData(localDatabaseData) {
        if (!localDatabaseData) {
            return;
        }
        // TODO create and configure db
        for (var key of Object.keys(localDatabaseData)) {
            this.localDatabase.setItem(key, localDatabaseData[key]);
        }
    }



}