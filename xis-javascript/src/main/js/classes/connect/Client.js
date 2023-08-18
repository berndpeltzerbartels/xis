/**
 * @typedef Client
 * @property {HttpClient} httpClient
 * @property {ClientConfig} config
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
     * @return {Promise<ClientConfig>}
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
     * @param {ClientData} clientData
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
    * @param {WidgewtInstance} widgetInstance
    * @returns {Promise<ServerReponse>}
    */
    loadWidgetData(widgetInstance) {
        var _this = this;
        var request = this.createWidgetRequest(widgetInstance, null);
        return this.httpClient.post('/xis/widget/model', request)
            .then(content => _this.deserializeResponse(content));
    }


    /**
     * @public
     * @param {WidgewtInstance} widgetInstance
     * @param {string} action
     * @returns {Promise<ServerReponse>}
     */
    widgetAction(widgetInstance, action) {
        var _this = this;
        var request = this.createWidgetRequest(widgetInstance, action);
        return this.httpClient.post('/xis/widget/action', request, {})
            .then(content => _this.deserializeResponse(content));
    }

    /**
     * @public
     * @param {string} pageId
     * @param {ClientData} clientData
     * @param {string} action
     * @returns {Promise<ServerReponse>}
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
     * @param {ClientData} pageClientData
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
        request.pathVariables = pageClientData.pathVariables;
        return request;
    }

    /**
    * @private
    * @param {string} widgetId 
    * @param {WidgetInstance} widgetInstance
    * @param {string} action
    * @param {string} targetContainerId
    * @returns {ClientRequest}
    */
    createWidgetRequest(widgetInstance, action) {
        var request = new ClientRequest();
        request.clientId = this.clientId;
        request.userId = this.userId;
        request.widgetId = widgetInstance.widget.id;
        request.action = action;
        request.data = widgetInstance.widgetState.data.values;
        request.urlParameters = widgetInstance.widgetState.resolvedURL.urlParameters;
        request.widgetParameters = widgetInstance.widgetState.widgetParameters;
        request.pathVariables = {};
        for (var pathVariable of widgetInstance.widgetState.resolvedURL.pathVariables) {
            var name = Object.keys(pathVariable)[0];
            var value = Object.values(pathVariable)[0];
            request.pathVariables[name] = value;
        }
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
     * @private
     * @param {string} content 
     * @returns {ServerReponse}
     */
    deserializeResponse(content) {
        var obj = JSON.parse(content);
        var data = obj.data ? new Data(JSON.parse(obj.data)) : new Data({});
        var response = new ServerResponse();
        response.data = data;
        response.nextPageURL = obj.nextPageURL;
        response.nextWidgetId = obj.nextWidgetId;
        return response;

    }
}
