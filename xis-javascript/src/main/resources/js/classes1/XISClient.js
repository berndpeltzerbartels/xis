class XISClient {

    /**
     * 
     * @param {XISRest} rest 
     */
    constructor(rest) {
        this.rest = rest;
        this.clientId = localStorage.getItem('xis-client-id');
        if (!this.clientId) {
            this.clientId = randomString(12);
            localStorage.setItem('xis-client-id', this.clientId);
        }
    }

    /**
     * @param {XISPage} page 
     * @returns {any} data-model from backend
     */
    loadPageModel(page) {
        this.rest.get('/xis/connector/load-model', {pageId: page.id, clientId: this.clientId});
    } 

}