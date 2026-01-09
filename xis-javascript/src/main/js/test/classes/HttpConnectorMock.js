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
                resolve(response);
            } catch (e) {
                reportError("Error during backend invocation: " + e);
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
                resolve(response);
            } catch (e) {
                reportError("Error during backend invocation: " + e);
                reject(e);
            }
        });
    }

    logRequest(method, uri, payload, headers) {
        console.log('method: ' + method);
        if (headers) {
            for (const key in headers) {
                console.log('header: ' + key + ' : ' + headers[key]);
            }
        }
    }
}