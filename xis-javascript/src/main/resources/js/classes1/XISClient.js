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
     * @public
     * @param {XISPage} page 
     * @returns {any} data-model from backend
     */
    onBind(page) {
        /*
        this.restClient.post(page.server + '/xis/connector/init', {pageId: page.id, clientId: this.clientId, token: this.token}), data => {
            page.processData(data);
            rootPage.refresh(page);
        };
        */
       debugger;
        page.setValues({title: 'Juchu!', 'font-size':'10px', 'font-family':'Arial'});    
        page.refresh(); // TODO remove debugcode
    } 

}