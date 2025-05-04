class SocketIOClient extends Client {

    /**
     * 
     * @param {SocketIOConnector} connector 
     */
    constructor(connector) {
        super();
        this.connector = connector;
    }
    /**
    * @public
    * @override
    * @return {Promise<ClientConfig>}
    */
    loadConfig() {
        throw new Error('Not implemented');
    }

    /**
     * @public
     å* @override
     * @param {string} pageId
     * @return {Promise<string>}
     */
    loadPageHead(pageId) {
        throw new Error('Not implemented');
    }


    /**
     * @public
     * @override
     * @param {string} pageId
     * @return {Promise<string>}
     */
    loadPageBody(pageId) {
        throw new Error('Not implemented');
    }

    /**
     * @public
     * @override
     * @param {string} pageId
     * @return {Promise<any>}
     */
    loadPageBodyAttributes(pageId) {
        throw new Error('Not implemented');
    }

    /**
    * @public
        * @override
    * @param {string} pageId
    * @return {Promise<string>}
    */
    loadWidget(widgetId) {
        throw new Error('Not implemented');
    }

    /**
     * @public
     * @override
     * @param {ResolvedURL} resolvedURL 
     * @returns {Promise<any>}
     */
    loadPageData(resolvedURL) {
        var _this = this;
        var request = this.createPageRequest(resolvedURL, null, null);
        return this.httpConnector.post('/xis/page/model', request, {})
            .then(response => _this.deserializeResponse(response));
    }

    /**
    * @public
    * @override
    * @param {WidgetInstance} widgetInstance
    * @returns {Promise<ServerReponse>}
    */
    loadWidgetData(widgetInstance, widgetState) {
        var _this = this;
        var request = this.createWidgetRequest(widgetInstance, widgetState, null, null, null);
        return this.httpConnector.post('/xis/widget/model', request)
            .then(response => _this.deserializeResponse(response));
    }

    /**
     * @public
     * @override
     * @param {ResolvedURL} resolvedURL 
     * @param {String} widgetId 
     * @param {String} formBindingKey 
     * @param {any} formBindingParameters 
     */
    loadFormData(resolvedURL, widgetId, formBindingKey, formBindingParameters) {
        throw new Error('Not implemented');
    }

    /**
     * @public
     * @override
     * @param {WidgetInstance} widgetInstance
     * @param {string} action
     * @param {string} actionParameters
     * @param {string} targetContainerId
     * @returns {Promise<ServerReponse>}
     */
    widgetLinkAction(widgetInstance, widgetState, action, actionParameters) {
        throw new Error('Not implemented');
    }

    /**
     * @public
     * @override
     * @param {ResolvedURL} resolvedURL 
     * @param {string} action
     * @param {any} actionParameters
     * @returns {Promise<ServerReponse>}
     */
    pageLinkAction(resolvedURL, action, actionParameters) {
        throw new Error('Not implemented');
    }

    /**
     * @public
     * @override
     * @param {ResolvedURL} resolvedURL 
     * @param {String} widgetId
     * @param {sring:string} formData
     * @param {string} action
     * @param {any} actionParameters
     * @param {string} binding
     * @returns {Promise<ServerReponse>}
     */
    formAction(resolvedURL, widgetId, formData, action, formBindigKey, formBindingParameters) {
        throw new Error('Not implemented');
    }

}