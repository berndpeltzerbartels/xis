/**
 * @typedef HttpClient
 * @property {HttpConnector} httpConnector
 * @property {ClientConfig} config
 * @property {string} clientId
 * @property {string} userId
 * @property {zoneId}
 */
class HttpClient extends Client{

    /**
     * @param {HttpConnector} httpConnector
     */
    constructor(httpConnector) {
        super();
        this.httpConnector = httpConnector;

        // TODO locale ?
    }

    /**
     * @public
     * @return {Promise<ClientConfig>}
     */
    loadConfig() {
        var _this = this;
        return this.httpConnector.get('/xis/config', {})
            .then(response => _this.deserializeConfig(response.responseText))
            .then(config => { _this.config = config; return config; });
    }

    /**
     * @public
     * @param {string} pageId
     * @return {Promise<string>}
     */
    loadPageHead(pageId) {
        return this.httpConnector.get('/xis/page/head', { uri: pageId }).then(response => response.responseText);
    }


    /**
     * @public
     * @param {string} pageId
     * @return {Promise<string>}
     */
    loadPageBody(pageId) {
        return this.httpConnector.get('/xis/page/body', { uri: pageId }).then(response => response.responseText);
    }

    /**
     * @public
     * @param {string} pageId
     * @return {Promise<any>}
     */
    loadPageBodyAttributes(pageId) {
        return this.httpConnector.get('/xis/page/body-attributes', { uri: pageId })
            .then(response => response.responseText)
            .then(content => JSON.parse(content));
    }

    /**
    * @public
    * @param {string} pageId
    * @return {Promise<string>}
    */
    loadWidget(widgetId) {
        return this.httpConnector.get('/xis/widget/html', { uri: widgetId }).then(response => response.responseText);
    }

    /**
     * @public
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
     * @param {ResolvedURL} resolvedURL 
     * @param {String} widgetId 
     * @param {String} formBindingKey 
     * @param {any} formBindingParameters 
     */
    loadFormData(resolvedURL, widgetId, formBindingKey, formBindingParameters) {
        var _this = this;
        var request = this.createFormRequest(resolvedURL, widgetId, {}, null, formBindingKey, formBindingParameters);
        return this.httpConnector.post('/xis/form/model', request)
            .then(response => _this.deserializeResponse(response));
    }

    /**
     * @public
     * @param {WidgetInstance} widgetInstance
     * @param {string} action
     * @param {string} actionParameters
     * @returns {Promise<ServerReponse>}
     */
    widgetLinkAction(widgetInstance, widgetState, action, actionParameters) {
        var _this = this;
        var request = this.createWidgetRequest(widgetInstance, widgetState, action, {}, actionParameters);
        return this.httpConnector.post('/xis/widget/action', request, {})
            .then(response => _this.deserializeResponse(response));
    }

    /**
     * @public
     * @param {ResolvedURL} resolvedURL 
     * @param {string} action
     * @param {any} actionParameters
     * @returns {Promise<ServerReponse>}
     */
    pageLinkAction(resolvedURL, action, actionParameters) {
        var _this = this;
        var request = this.createPageRequest(resolvedURL, {}, action, actionParameters);
        return this.httpConnector.post('/xis/page/action', request, {})
            .then(response => _this.deserializeResponse(response));
    }

    /**
     * @public
     * @param {ResolvedURL} resolvedURL 
     * @param {String} widgetId
     * @param {sring:string} formData
     * @param {string} action
     * @param {any} actionParameters
     * @param {string} binding
     * @returns {Promise<ServerReponse>}
     */
    formAction(resolvedURL, widgetId, formData, action, formBindigKey, formBindingParameters) {
        var _this = this;
        var request = this.createFormRequest(resolvedURL, widgetId, formData, action, formBindigKey, formBindingParameters);
        return this.httpConnector.post('/xis/form/action', request, {})
            .then(response => _this.deserializeResponse(response));
    }




}
