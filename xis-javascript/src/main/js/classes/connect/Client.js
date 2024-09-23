/**
 * @typedef Client
 * @property {HttpClient} httpClient
 * @property {ClientConfig} config
 * @property {string} clientId
 * @property {string} userId
 * @property {zoneId}
 */
class Client {

    /**
     * @param {HttpClient} httpClient
     */
    constructor(httpClient) {
        this.httpClient = httpClient;
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
        return this.httpClient.get('/xis/config', {})
            .then(response => _this.deserializeConfig(response.responseText))
            .then(config => { _this.config = config; return config; });
    }

    /**
     * @public
     * @param {string} pageId
     * @return {Promise<string>}
     */
    loadPageHead(pageId) {
        return this.httpClient.get('/xis/page/head', { uri: pageId }).then(response => response.responseText);
    }


    /**
     * @public
     * @param {string} pageId
     * @return {Promise<string>}
     */
    loadPageBody(pageId) {
        return this.httpClient.get('/xis/page/body', { uri: pageId }).then(response => response.responseText);
    }

    /**
     * @public
     * @param {string} pageId
     * @return {Promise<any>}
     */
    loadPageBodyAttributes(pageId) {
        return this.httpClient.get('/xis/page/body-attributes', { uri: pageId })
            .then(response => response.responseText)
            .then(content => JSON.parse(content));
    }

    /**
    * @public
    * @param {string} pageId
    * @return {Promise<string>}
    */
    loadWidget(widgetId) {
        return this.httpClient.get('/xis/widget/html', { uri: widgetId }).then(response => response.responseText);
    }

    /**
     * @public
     * @param {ResolvedURL} resolvedURL 
     * @returns {Promise<any>}
     */
    loadPageData(resolvedURL) {
        var _this = this;
        var request = this.createPageRequest(resolvedURL, null, null);
        return this.httpClient.post('/xis/page/model', request, {})
            .then(response => _this.deserializeResponse(response));
    }

    /**
    * @public
    * @param {string} widgetId
    * @param {WidgetInstance} widgetInstance
    * @returns {Promise<ServerReponse>}
    */
    loadWidgetData(widgetInstance, widgetState) {
        var _this = this;
        var request = this.createWidgetRequest(widgetInstance, widgetState, null, null, null);
        return this.httpClient.post('/xis/widget/model', request)
            .then(response => _this.deserializeResponse(response));
    }


    /**
     * @public
     * @param {WidgetInstance} widgetInstance
     * @param {string} action
     * @param {string} actionParameters
     * @returns {Promise<ServerReponse>}
     */
    widgetAction(widgetInstance, widgetState, action, formData, actionParameters) {
        var _this = this;
        var request = this.createWidgetRequest(widgetInstance, widgetState, action, formData, actionParameters);
        return this.httpClient.post('/xis/widget/action', request, {})
            .then(response => _this.deserializeResponse(response));
    }

    /**
     * @public
     * @param {ResolvedURL} resolvedURL 
     * @param {Data} formData
     * @param {string} action
     * @param {any} actionParameters
     * @returns {Promise<ServerReponse>}
     */
    pageAction(resolvedURL, formData, action, actionParameters) {
        var _this = this;
        var request = this.createPageRequest(resolvedURL, formData, action, actionParameters);
        return this.httpClient.post('/xis/page/action', request, {})
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
        request.widgetParameters = widgetState.widgetParameters;
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
        var serverResponse = new ServerResponse();
        serverResponse.data = data;
        serverResponse.nextPageURL = obj.nextPageURL;
        serverResponse.nextWidgetId = obj.nextWidgetId;
        serverResponse.status = response.status;
        data.setValue(['validation'], obj.validatorMessages);
        return serverResponse;

    }



}
