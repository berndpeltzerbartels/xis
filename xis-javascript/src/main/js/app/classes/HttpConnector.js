class HttpConnector {

    /**
     * @param {string} clientId
     */
    constructor(clientId) {
        this.clientId = clientId;
    }

    /**
     * @public
     * @param {string} uri
     * @param {any} payload
     * @return {Promise<any, int>}
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
     * @return {Promise<any, int>}
     */
    get(uri, headers) {
        return this.doRequest(uri, headers, 'GET', undefined);
    }

    /**
     * @public
     * @param {string} renewToken 
     * @return {Promise<any, int>}
     * @throws {Error} If the request fails or the response does not contain the expected token data.
     */
    sendRenewTokenRequest(renewToken) {
        return this.httpConnector.post('/xis/token/renew', {}, {});
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
        if (headers == null) {
            headers = {};
        }
        for (var name of Object.keys(headers)) {
            xmlHttp.setRequestHeader(name, headers[name]);
        }
        var promise = new Promise((resolve, reject) => {
            xmlHttp.onreadystatechange = function () {
                if (xmlHttp.status === 0) {
                    // NICHT auflösen, auf onerror warten!
                    return;
                }
                // TODO Handle errors and "304 NOT MODIFIED"
                // TODO Add headers to allow 304
                // Readystaet == 4 for 304 ?
                if (xmlHttp.readyState == 4) { // TODO In Java 204 if there is no server-method
                    resolve(xmlHttp);
                }
                // TODO use errorhandler
            }
            xmlHttp.onerror = function (e) {
                reportError('Error during HTTP request to ' + uri, e);
                reject(xmlHttp);
            };

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
