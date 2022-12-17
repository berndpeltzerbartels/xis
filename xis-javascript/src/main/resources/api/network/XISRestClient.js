class XISRestClient {

    /**
     * 
     * @param {XISHttpClient} httpClient 
     */
    constructor(httpClient) {
        this.className = 'XISRestClient';
        this.httpClient = httpClient;
        this.headers = {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        }
    }

    /**
     * @param {string} uri
     * @param {any} customHeaders
     * @param {any} payload
     * @param {Function} handler 
     */
    post(uri, customHeaders, payload, handler) {
        this.httpClient.post(uri, this.getAllHeaders(customHeaders), payload, handler);
    }


    /**
     * @private
     * @param {any} customHeaders
     * @returns {any}
     */
    getAllHeaders(customHeaders) {
        var composedHeaders = {};
        for (var key in this.headers) {
            composedHeaders[key] = this.headers[key];
        }
        for (var key in customHeaders) {
            composedHeaders[key] = customHeaders[key];
        }
        return composedHeaders;
    }


}