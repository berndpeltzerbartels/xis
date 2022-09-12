class ProductListClient extends XISClient {

    /**
     * 
     * @param {XISRestClient} restClient 
     */
    constructor(errorHandler) {
        super(errorHandler); // TODO Better to do everything socket.io ?
    }

    init() {
        // doRequest(uri, method, payload, handler) {
        this.restClient.doRequest('/init', 'POST', {
            'headers': {
                clientId: this.clientId,
                token: this.token,
                intention: 'init'
            },
            state: {}

        })

    }

}