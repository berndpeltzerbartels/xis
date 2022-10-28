class XISClient {

    /**
     * @param {XISErrorHandler} errorHandler 
     */
    constructor(errorHandler) {
        this.restClient = new XISRestClient(errorHandler);
        this.token = localStorage.getItem('xis-token');
        this.clientId = localStorage.getItem('xis-client-id');
        if (!this.clientId) {
            this.clientId = randomString(12);
            localStorage.setItem('xis-client-id', this.clientId);
        }
    }

    callRemoteInit(signatures, component) {
        this.callRemote(signatures, component);
    }

    callRemote(signatures, component, issue){
        var message = {
            signatures: signatures,
            state: component.state,
            clientId: this.clientId,
            token: this.token,
            issue: issue,
            javaClassId: component.javaClassId,
            componentType: component.type
        };
        this.restClient.post('/xis/connector', message, response => component.updateState(response.model));
    }

    /**
     * @public
     * @param {XISPage} page 
     * @returns {any} data-model from backend
     */
    init(page) {

        this.restClient.post(page.server + '/xis/connector/init', {pageId: page.id, clientId: this.clientId, token: this.token}), data => {
            page.processData(data);
            rootPage.refresh(page);
        };
    
       debugger;
        page.setValues({title: 'Juchu!', 'font-size':'10px', 'font-family':'Arial'});    
        page.refresh(); // TODO remove debugcode
    } 

}