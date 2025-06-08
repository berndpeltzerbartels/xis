/**
 * @typedef HttpClient
 * @property {HttpConnector} httpConnector
 * @property {ClientConfig} config
 * @property {string} clientId
 * @property {zoneId}
 */
class HttpClient extends Client{

    /**
     * @param {HttpConnector} httpConnector
     */
    constructor(httpConnector, tokenManager) {
        super(tokenManager);
        this.httpConnector = httpConnector;

        // TODO locale ?
    }

    /**
     * @public
     * @override
     * @return {Promise<ClientConfig>}
     */
    loadConfig() {
        var _this = this;
        return this.httpConnector.get('/xis/config', this.authenticationHeader())
            .then(response => _this.deserializeConfig(response.responseText))
            .then(config => { _this.config = config; return config; });
    }

    /**
     * @public
     * @override
     * @param {string} pageId
     * @return {Promise<string>}
     */
    loadPageHead(pageId) {
        var headers = this.authenticationHeader();
        headers.uri = pageId;
        return this.httpConnector.get('/xis/page/head', headers).then(response => response.responseText);
    }


    /**
     * @public
     * @override
     * @param {string} pageId
     * @return {Promise<string>}
     */
    loadPageBody(pageId) {
        var headers = this.authenticationHeader();
        headers.uri = pageId;
        return this.httpConnector.get('/xis/page/body', headers).then(response => response.responseText);
    }

    /**
     * @public
     * @override
     * @param {string} pageId
     * @return {Promise<any>}
     */
    loadPageBodyAttributes(pageId) {
        var headers = this.authenticationHeader();
        headers.uri = pageId;
        return this.httpConnector.get('/xis/page/body-attributes', headers)
            .then(response => response.responseText)
            .then(content => JSON.parse(content));
    }

    /**
    * @public
    * @override
    * @param {string} pageId
    * @return {Promise<string>}
    */
    loadWidget(widgetId) {
        var headers = this.authenticationHeader();
        headers.uri = widgetId;
        return this.httpConnector.get('/xis/widget/html', headers).then(response => response.responseText);
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
        return this.httpConnector.post('/xis/page/model', request, this.authenticationHeader())
            .then(response => _this.deserializeResponse(response));
    }

    /**
    * @public
    * @override
    * @param {WidgetInstance} widgetInstance
    * @returns {Promise<ServerResponse>}
    */
    loadWidgetData(widgetInstance, widgetState) {
        var _this = this;
        var request = this.createWidgetRequest(widgetInstance, widgetState, null, null, null);
        return this.httpConnector.post('/xis/widget/model', request, this.authenticationHeader)
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
        var _this = this;
        var request = this.createFormRequest(resolvedURL, widgetId, {}, null, formBindingKey, formBindingParameters);
        return this.httpConnector.post('/xis/form/model', request, this.authenticationHeader())
            .then(response => _this.deserializeResponse(response));
    }

    /**
     * @public
     * @override
     * @param {WidgetInstance} widgetInstance
     * @param {string} action
     * @param {string} actionParameters
     * @returns {Promise<ServerResponse>}
     */
    widgetLinkAction(widgetInstance, widgetState, action, actionParameters) {
        var _this = this;
        var request = this.createWidgetRequest(widgetInstance, widgetState, action, {}, actionParameters);
        return this.httpConnector.post('/xis/widget/action', request, this.authenticationHeader())
            .then(response => _this.deserializeResponse(response));
    }

    /**
     * @public
     * @override
     * @param {ResolvedURL} resolvedURL 
     * @param {string} action
     * @param {any} actionParameters
     * @returns {Promise<ServerResponse>}
     */
    pageLinkAction(resolvedURL, action, actionParameters) {
        var _this = this;
        var request = this.createPageRequest(resolvedURL, {}, action, actionParameters);
        return this.httpConnector.post('/xis/page/action', request, this.authenticationHeader())
            .then(response => _this.deserializeResponse(response));
    }

    /**
     * @public
     * @override
     * @param {ResolvedURL} resolvedURL 
     * @param {String} widgetId
     * @param {string:string} formData
     * @param {string} action
     * @param {any} actionParameters
     * @param {string} binding
     * @returns {Promise<ServerResponse>}
     */
    formAction(resolvedURL, widgetId, formData, action, formBindigKey, formBindingParameters) {
        var _this = this;
        var request = this.createFormRequest(resolvedURL, widgetId, formData, action, formBindigKey, formBindingParameters);
        return this.httpConnector.post('/xis/form/action', request, this.authenticationHeader())
            .then(response => _this.deserializeResponse(response));
    }

    /**
     * @public
     * @override
     * @param {string} renewToken
     * @returns {Promise<TokenResponse>}
     */
    sendRenewTokenRequest(renewToken) {
        var _this = this;
        return this.httpConnector.post('/xis/token/renew', {}, this.authenticationHeaderForRenew())
            .then(response => _this.deserializeResponse(response));
    }

    /**
     * @private
     * Returns the authentication header for the current user.
     * This header is used for all requests that require authentication.
     * 
     * @returns {Object} The authentication header with the access token.
     */
    authenticationHeader() {
        var header = {};
        var accessToken = this.tokenManager.actualAccessToken();
        if (accessToken) {
            header['Authorization'] = 'Bearer ' + accessToken;
        }
        return header;
    }
    /**
     * @private
     * Returns the authentication header for renewing the token.
     * This header is used when the access token has expired and needs to be renewed.
     * 
     * @returns {Object} The authentication header with the renew token.
     */
    authenticationHeaderForRenew() {
        return {
            'Authorization': 'Bearer '+ this.tokenManager.renewToken
        };
    }
}   
