/**
 * @typedef Client
 * @property {HttpClient} httpClient
 * @property {Config} config
 * @property {string} clientId
 * @property {string} userId
 */
class Client {

    /**
     * @param {HttpClient} httpClient
     */
    constructor(httpClient) {
        this.httpClient = httpClient;
        this.config = undefined;
        this.clientId = '';
        this.userId = '';
    }

    /**
     * @public
     * @return {Promise<Config>}
     */
    loadConfig() {
        var _this = this;
        return this.httpClient.get('/xis/config', {})
            .then(content => _this.deserializeConfig(content));
    }

    /**
     * @public
     * @param {string} pageId
     * @return {Promise<string>}
     */
    loadPageHead(pageId) {
        return this.httpClient.get('/xis/page/head', { uri: pageId });
    }


    /**
     * @public
     * @param {string} pageId
     * @return {Promise<string>}
     */
    loadPageBody(pageId) {
        return this.httpClient.get('/xis/page/body', { uri: pageId });
    }

    /**
     * @public
     * @param {string} pageId
     * @return {Promise<any>}
     */
    loadPageBodyAttributes(pageId) {
        return this.httpClient.get('/xis/page/body-attributes', { uri: pageId }).then(content => JSON.parse(content));
    }

    /**
    * @public
    * @param {string} pageId
    * @return {Promise<string>}
    */
    loadWidget(widgetId) {
        return this.httpClient.get('/xis/widget/html/' + widgetId, {});
    }

    /**
     * @public
     * @param {string} pageId
     * @param {PageClientData} clientData
     * @returns {Promise<any>}
     */
    loadPageData(pageId, clientData) {
        var _this = this;
        var request = this.createPageRequest(pageId, clientData, null);
        return this.httpClient.post('/xis/page/model', request, {})
            .then(content => _this.deserializeResponse(content));
    }

    /**
    * @public
    * @param {string} widgetId
    * @param {WidgetClientData} widgetClientData
    * @returns {Promise<Response>}
    */
    loadWidgetData(widgetId, widgetClientData) {
        var _this = this;
        var request = this.createWidgetRequest(widgetId, widgetClientData, null);
        return this.httpClient.post('/xis/widget/model', request)
            .then(content => _this.deserializeResponse(content));
    }


    /**
     * @public
     * @param {string} widgetId
     * @param {WidgetClientData} widgetClientData
     * @param {string} action
     * @returns {Promise<Response>}
     */
    widgetAction(widgetId, widgetClientData, action) {
        var _this = this;
        var request = this.createWidgetRequest(widgetId, widgetClientData, action);
        return this.httpClient.post('/xis/widget/action', request, {})
            .then(content => _this.deserializeResponse(content));
    }

    /**
     * @public
     * @param {string} pageId
     * @param {PageClientData} clientData
     * @param {string} action
     * @returns {Promise<Response>}
     */
    pageAction(pageId, pageClientData, action) {
        var _this = this;
        var request = this.createPageRequest(pageId, pageClientData, action);
        return this.httpClient.post('/xis/page/action', request, {})
            .then(content => _this.deserializeResponse(content));
    }

    /**
     * @private
     * @param {string} pageId 
     * @param {PageClientData} pageClientData
     * @param {string} action
     * @returns {ClientRequest}
     */
    createPageRequest(pageId, pageClientData, action) {
        var request = new ClientRequest();
        request.clientId = this.clientId;
        request.userId = this.userId;
        request.pageId = pageId;
        request.data = pageClientData.modelData;
        request.action = action;
        request.urlParameters = pageClientData.urlParameters;
        request.pathVariables = {};
        for (var pathVariable of pageClientData.pathVariables) {
            var name = Object.keys(pathVariable)[0];
            var value = Object.values(pathVariable)[0];
            request.pathVariables[name] = value;
        }
        request.parameters = {};
        for (var parameter of pageClientData.parameters) {
            request.parameters[parameter.name] = parameter.value;
        }
        return request;
    }

    /**
    * @private
    * @param {string} widgetId 
    * @param {WidgetClientData} widgetClientData
    * @param {string} action
    * @returns {ClientRequest}
    */
    createWidgetRequest(widgetId, widgetClientData, action) {
        var request = new ClientRequest();
        request.clientId = this.clientId;
        request.userId = this.userId;
        request.widgetId = widgetId;
        request.action = action;
        request.data = widgetClientData.modelData;
        request.data = widgetClientData.modelData;
        request.parameters = widgetClientData.parameters;
        return request;
    }


    /**
     * @private
     * @param {string} content 
     * @returns {Config}
     */
    deserializeConfig(content) {
        var obj = JSON.parse(content);
        var config = new Config();
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
     * @private
     * @param {string} content 
     * @returns {Response}
     */
    deserializeResponse(content) {
        var obj = JSON.parse(content);
        var data = obj.data ? new Data(obj.data) : new Data({});
        var response = new ServerResponse();
        response.data = data;
        response.nextPageURL = obj.nextPageURL;
        response.nextWidgetId = obj.nextWidgetId;
        return response;

    }
}
