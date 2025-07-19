class HttpConnectorMock {

    /**
     * @public
     * @param {string} uri
     * @param {any} payload
     * @param {any} headers
     * @return {Promise<any,int>}
     */
    post(uri, payload, headers) {
        this.logRequest('POST', uri, payload, headers);
        return new Promise((resolve, reject) => {
            try {
                // 'backendBridge' wird vom IntegrationTestContext bereitgestellt
                const response = backendBridge.invokeBackend(
                    'POST',
                    uri,
                    headers || {},
                    JSON.stringify(payload || {})
                );
                this.logResponse(response);
                resolve(response);
            } catch (e) {
                console.error("Error during backend invocation: " + e);
                reject(e);
            }
        });
    }

    /**
     * @public
     * @param {string} uri
     * @param {any} headers
     * @return {Promise<any, int>}
     */
    get(uri, headers) {
        this.logRequest('GET', uri, {}, headers);
        return new Promise((resolve, reject) => {
            try {
                // 'backendBridge' wird vom IntegrationTestContext bereitgestellt
                const response = backendBridge.invokeBackend(
                    'GET',
                    uri,
                    headers || {},
                    null
                );
                this.logResponse(response);
                resolve(response);
            } catch (e) {
                console.error("Error during backend invocation: " + e);
                reject(e);
            }
        });
    }

    logRequest(method, uri, payload, headers) {
        console.log('---------------------------------request-------------------------------------');
        console.log('method: ' + method);
        if (headers) {
            for (const key in headers) {
                console.log('header: ' + key + ' : ' + headers[key]);
            }
        }
        console.log('uri: ' + uri);
        console.log('payload: ' + JSON.stringify(payload || {}));
    }

    logResponse(response) {
        console.log('----------------------------------response------------------------------------');
        // Die 'response' ist das JavascriptResponse-Objekt aus Java
        console.log('status: ' + response.status);
        console.log('response body: ' + response.responseText);
    }
}