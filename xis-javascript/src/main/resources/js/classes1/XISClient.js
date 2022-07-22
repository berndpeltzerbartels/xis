class XISClient {

    /**
     * 
     * @param {XISRestClient} restClient 
     */
    constructor(restClient) {
        this.restClient = restClient;
        this.token = localStorage.getItem('xis-token');
        this.clientId = localStorage.getItem('xis-client-id');
        if (!this.clientId) {
            this.clientId = randomString(12);
            localStorage.setItem('xis-client-id', this.clientId);
        }
    }

    /**
     * @param {XISRootPage} rootPage
     * @param {XISPage} page 
     * @returns {any} data-model from backend
     */
    loadPageModel(rootPage, page) {
        this.restClient.post(page.server + '/xis/connector/load-model', {pageId: page.id, clientId: this.clientId, token: this.token}), data => {
            page.processData(data);
            rootPage.refresh(page);
        };
    } 

}