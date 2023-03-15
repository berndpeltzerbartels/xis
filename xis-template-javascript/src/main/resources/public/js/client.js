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
     * @param {Data} data
     * @returns {Promise<any>}
     */
    loadPageData(pageId, data) {
        var request = this.createRequest(pageId, data, undefined);
        return this.httpClient.post('/xis/page/model', request, {})
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
        return this.httpClient.post('/xis/page/model', request)
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
        return this.httpClient.post('/xis/widget/action', request, {})
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
        return this.httpClient.post('/xis/page/action', request, {});
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


class HttpClient {

    /**
     * @param {Function} errorHandler 
     */
    constructor(errorHandler) {
        this.className = 'HttpClient';
        this.errorHandler = errorHandler;
    }

    /**
     * @public
     * @param {string} uri
     * @param {any} payload 
     * @return {Promise<any>}
     * 
     */
    post(uri, payload, headers) {
        if (!headers) headers = {};
        var payloadJson = JSON.stringify(payload);
        headers['Content-type'] = 'application/json';
        return this.doRequest(uri, headers, 'POST', payloadJson);
    }

    /**
     * @public
     * @param {string} uri 
     * @param {any} headers
     * @return {Promise<any>}
     */
    get(uri, headers) {
        return this.doRequest(uri, headers, 'GET', undefined);
    }

    /**
     * @private
     * @param {string} uri 
     * @param {any} headers
     * @param {string} method 
     * @param {any} payload 
     * @return {Promise<any>}
     */
    doRequest(uri, headers, method, payload) {
        var xmlHttp = new XMLHttpRequest();
        xmlHttp.open(method, uri, true); // true for asynchronous
        for (var name of Object.keys(headers)) {
            xmlHttp.setRequestHeader(name, headers[name]);
        }
        var promise = new Promise((resolve, reject) => {
            xmlHttp.onreadystatechange = function () {
                // TODO Handle errors and "304 NOT MODIFIED"
                // TODO Add headers to allow 304
                // Readystaet == 4 for 304 ?
                if (xmlHttp.readyState == 4) { // TODO In Java 204 if there is no server-method
                    if (xmlHttp.status == 200) {
                        resolve(xmlHttp.responseText);
                    } else {
                        reject('status: ' + xmlHttp.status);
                    }
                }
                // TODO use errorhandler
            }

        });

        if (payload) {
            xmlHttp.send(payload);
        }
        else {
            xmlHttp.send();
        }
        return promise;
    }

}
