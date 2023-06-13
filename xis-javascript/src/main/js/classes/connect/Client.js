/**
 * @property {HttpClient} httpClient
 * @property {Config} config
 * @property {string} clientId
 * @property {string} userId
 */
class Client {

    /**
     *
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
     * @return {Promise<any>}
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
     * @param {any} data
     * @param {any} parameters 
     * @returns {Promise<any>}
     */
    loadPageData(pageId, data, parameters) {
        var request = this.createDataRequest(pageId, null, data, parameters);
        return this.httpClient.post('/xis/page/model', request, {})
            .then(content => JSON.parse(content));
    }

    /**
     * @public
     * @param {string} widgetId
     * @param {any} data
     * @param {any} parameters
     * @returns {Promise<any>}
     */
    loadWidgetData(widgetId, data, parameters) {
        var request = this.createDataRequest(null, widgetId, data, parameters);
        return this.httpClient.post('/xis/widget/model', request)
            .then(content => JSON.parse(content));
    }


    /**
     * @public
     * @param {string} widgetId
     * @param {string} action
     * @param {Data} data
     * @returns {Promise<Response>}
     */
    widgetAction(widgetId, action, data) {
        var request = this.createDataRequest(null, widgetId, data, action);
        return this.httpClient.post('/xis/widget/action', request, {})
            .then(content => JSON.parse(content));
    }

    /**
     * @public
     * @param {string} pageId
     * @param {string} action
     * @param {Data} data
     * @returns {Promise<Response>}
     */
    pageAction(pageId, action, data) {
        var request = this.createDataRequest(pageId, null, data, action);
        return this.httpClient.post('/xis/page/action', request, {})
            .then(content => JSON.parse(content));
    }

    /**
    * @private
    * @param {string} pageId
    * @param {string} widgetId
    * @param {any} data
    * @param {any} parameters
    */
    createDataRequest(pageId, widgetId, data, parameters) {
        debugger;
        var request = new Request();
        request.clientId = this.clientId;
        request.userId = this.userId;
        request.pageId = pageId;
        request.widgetId = widgetId;
        request.data = data;
        request.parameters = parameters;
        return request;
    }

    /**
    * @private
    * @param {string} pageId
    * @param {string} widgetId
    * @param {any} data
    * @param {any} parameters
    * @param {string} nextPageId
    * @param {string} nextWidgetId
    */
    createActionRequest(pageId, widgetId, data, action, parameters, nextPageId, nextWidgetId) {
        var request = new Request();
        request.clientId = this.clientId;
        request.userId = this.userId;
        request.pageId = pageId;
        request.widgetId = widgetId;
        request.action = action;
        request.data = data;
        request.parameters = parameters;
        request.nextPageId = nextPageId;
        request.nextWidgetId = nextWidgetId;
        return request;
    }
}
