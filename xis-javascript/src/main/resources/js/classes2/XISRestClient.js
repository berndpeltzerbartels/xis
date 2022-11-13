class XISRestClient {

    /**
     * @param {XISErrorHandler} errorHandler 
     */
    constructor(errorHandler) {
        this.className = 'XISRestClient';
        this.httpClient = new XISHttpClient(errorHandler);
        this.errorHandler = errorHandler;
        this.headers =  {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        }
    }

    /**
     * @param {string} uri
     * @param {any} payload
     * @param {Function} handler 
     */
    post(uri, payload, handler) {
        this.httpClient.post(uri, this.headers, payload, handler);        
    }
  
    
}