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
     * @returns {Promise<any>}
     */
    loadPageData(pageId, data) {
        var request = this.createRequest(pageId, null, data, undefined);
        return this.httpClient.post('/xis/page/model', request, {})
            .then(content => JSON.parse(content));
    }

    /**
     * @public
     * @param {string} widgetId
     * @param {any} data
     * @returns {Promise<any>}
     */
    loadWidgetData(widgetId, data) {
        var request = this.createRequest(null, widgetId, data, undefined);
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
        var request = this.createRequest(null, widgetId, data, action);
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
        var request = this.createRequest(pageId, null, data, action);
        return this.httpClient.post('/xis/page/action', request, {});
    }


    /**
    * @private
    * @param {string} pageId
    * @param {string} widgetId
    * @param {any} data
    */
    createRequest(pageId, widgetId, data, action) {
        var request = new ComponentRequest();
        request.clientId = this.clientId;
        request.userId = this.userId;
        request.pageId = pageId;
        request.widgetId = widgetId;
        request.action = action;
        request.data = data;
        return request;
    }
}
