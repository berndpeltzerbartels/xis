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
    constructor() {
        this.config = undefined;
        this.clientId = '';
        this.userId = '';
    }

    /**
     * @public
     * @return {Promise<any>}
     */
    loadConfig() {
        return httpClient.get('/xis/config', {})
            .then(content => JSON.parse(content));
    }

    /**
     * @public
     * @param {string} pageId
     * @return {Promise<string>}
     */
    loadPageHead(pageId) {
        return httpClient.get('/xis/page/head', { uri: pageId });
    }


    /**
     * @public
     * @param {string} pageId
     * @return {Promise<string>}
     */
    loadPageBody(pageId) {
        return httpClient.get('/xis/page/body', { uri: pageId });
    }

    /**
     * @public
     * @param {string} pageId
     * @return {Promise<any>}
     */
    loadPageBodyAttributes(pageId) {
        return httpClient.get('/xis/page/body-attributes', { uri: pageId }).then(content => JSON.parse(content));
    }

    /**
    * @public
    * @param {string} pageId
    * @return {Promise<string>}
    */
    loadWidget(widgetId) {
        return httpClient.get('/xis/widget/html/' + widgetId, {});
    }

    /**
     * @public
     * @param {string} pageId
     * @param {Data} data
     * @returns {Promise<any>}
     */
    loadPageData(pageId, data) {
        var request = this.createRequest(pageId, data, undefined);
        return httpClient.post('/xis/page/model', request, {})
            .then(content => JSON.parse(content));
    }

    /**
     * @public
     * @param {string} widgetId
     * @param {Data} data
     * @returns {Promise<any>}
     */
    loadWidgetData(widgetId, data) {
        var request = this.createRequest(widgetId, data, undefined);
        return httpClient.post('/xis/widget/model', request)
            .then(content => JSON.parse(content));
    }

    /**
     * @public
     * @param {string} widgetId
     * @param {string} action
     * @param {Data} data
     * @returns {Promise<any>}
     */
    widgetAction(widgetId, action, data) {
        var request = this.createRequest(widgetId, data, action);
        return httpClient.post('/xis/widget/action', request, {})
            .then(content => JSON.parse(content));
    }

    /**
     * @public
     * @param {string} pageId
     * @param {string} action
     * @param {Data} data
     * @returns {Promise<any>}
     */
    pageAction(pageId, action, data) {
        var request = this.createRequest(pageId, data, action);
        return httpClient.post('/xis/page/action', request, {});
    }


    /**
    * @private
    * @param {string} controllerId
    * @param {any} data
    */
    createRequest(controllerId, data, action) {
        var request = new ComponentRequest();
        request.clientId = this.clientId;
        request.userId = this.userId;
        request.controllerId = controllerId;
        request.action = action;
        request.data = data;
        return request;
    }
}
