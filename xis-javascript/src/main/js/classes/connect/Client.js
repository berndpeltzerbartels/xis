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
        return this.httpClient.get('/xis/config', {})
            .then(content => JSON.parse(content));
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
        var request = this.createPageRequest(pageId, clientData, null);
        return this.httpClient.post('/xis/page/model', request, {})
            .then(content => JSON.parse(content));
    }

    /**
    * @public
    * @param {string} widgetId
    * @param {WidgetClientData} widgetClientData
    * @returns {Promise<Response>}
    */
    loadWidgetData(widgetId, widgetClientData) {
        var request = this.createWidgetRequest(widgetId, widgetClientData, null);
        return this.httpClient.post('/xis/widget/model', request)
            .then(content => JSON.parse(content));
    }


    /**
     * @public
     * @param {string} widgetId
     * @param {WidgetClientData} widgetClientData
     * @param {string} action
     * @returns {Promise<Response>}
     */
    widgetAction(widgetId, widgetClientData, action) {
        var request = this.createWidgetRequest(widgetId, widgetClientData, action);
        return this.httpClient.post('/xis/widget/action', request, {})
            .then(content => JSON.parse(content));
    }

    /**
     * @public
     * @param {string} pageId
     * @param {PageClientData} clientData
     * @param {string} action
     * @returns {Promise<Response>}
     */
    pageAction(pageId, pageClientData, action) {
        var request = this.createPageRequest(pageId, pageClientData, action);
        return this.httpClient.post('/xis/page/action', request, {})
            .then(content => JSON.parse(content));
    }

    /**
     * @private
     * @param {string} pageId 
     * @param {PageClientData} pageClientData
     * @param {string} action
     * @returns {Request}
     */
    createPageRequest(pageId, pageClientData, action) {
        debugger;
        var request = new Request();
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
    * @returns {Request}
    */
    createWidgetRequest(widgetId, widgetClientData, action) {
        var request = new Request();
        request.clientId = this.clientId;
        request.userId = this.userId;
        request.widgetId = widgetId;
        request.action = action;
        request.data = widgetClientData.modelData;
        request.data = widgetClientData.modelData;
        request.parameters = widgetClientData.parameters;
        return request;
    }

}
