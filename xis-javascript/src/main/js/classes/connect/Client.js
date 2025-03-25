/**
 * @typedef Client
 * @property {HttpConnector} httpConnector
 * @property {ClientConfig} config
 * @property {string} clientId
 * @property {string} userId
 * @property {zoneId}
 */
class Client {

    /**
     * @param {HttpConnector} httpConnector
     */
    constructor(httpConnector) {
        this.httpConnector = httpConnector;
        this.config = undefined;
        this.clientId = randomString();
        this.userId = '';
        this.zoneId = timeZone();
        // TODO locale ?
    }

    /**
     * @public
     * @return {Promise<ClientConfig>}
     */
    loadConfig() {
        var _this = this;
        return this.httpConnector.get('/xis/config', {})
            .then(response => _this.deserializeConfig(response.responseText))
            .then(config => { _this.config = config; return config; });
    }

    /**
     * @public
     * @param {string} pageId
     * @return {Promise<string>}
     */
    loadPageHead(pageId) {
        return this.httpConnector.get('/xis/page/head', { uri: pageId }).then(response => response.responseText);
    }


    /**
     * @public
     * @param {string} pageId
     * @return {Promise<string>}
     */
    loadPageBody(pageId) {
        return this.httpConnector.get('/xis/page/body', { uri: pageId }).then(response => response.responseText);
    }

    /**
     * @public
     * @param {string} pageId
     * @return {Promise<any>}
     */
    loadPageBodyAttributes(pageId) {
        return this.httpConnector.get('/xis/page/body-attributes', { uri: pageId })
            .then(response => response.responseText)
            .then(content => JSON.parse(content));
    }

    /**
    * @public
    * @param {string} pageId
    * @return {Promise<string>}
    */
    loadWidget(widgetId) {
        return this.httpConnector.get('/xis/widget/html', { uri: widgetId }).then(response => response.responseText);
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
        var _this = this;
        var request = this.createFormRequest(resolvedURL, widgetId, {}, null, formBindingKey, formBindingParameters);
        return this.httpConnector.post('/xis/form/model', request)
            .then(response => _this.deserializeResponse(response));
    }

    /**
     * @public
     * @param {WidgetInstance} widgetInstance
     * @param {string} action
     * @param {string} actionParameters
     * @returns {Promise<ServerReponse>}
     */
    widgetLinkAction(widgetInstance, widgetState, action, actionParameters) {
        var _this = this;
        var request = this.createWidgetRequest(widgetInstance, widgetState, action, {}, actionParameters);
        return this.httpConnector.post('/xis/widget/action', request, {})
            .then(response => _this.deserializeResponse(response));
    }

    /**
     * @public
     * @param {ResolvedURL} resolvedURL 
     * @param {string} action
     * @param {any} actionParameters
     * @returns {Promise<ServerReponse>}
     */
    pageLinkAction(resolvedURL, action, actionParameters) {
        var _this = this;
        var request = this.createPageRequest(resolvedURL, {}, action, actionParameters);
        return this.httpConnector.post('/xis/page/action', request, {})
            .then(response => _this.deserializeResponse(response));
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
        var _this = this;
        var request = this.createFormRequest(resolvedURL, widgetId, formData, action, formBindigKey, formBindingParameters);
        return this.httpConnector.post('/xis/form/action', request, {})
            .then(response => _this.deserializeResponse(response));
    }


    /**
     * @private
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
        return request;
    }

    /**
     * @private
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
        return request;
    }

    /**
    * @private
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
        return request;
    }


    /**
     * @private
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
     * @private
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
        return serverResponse;

    }



}
