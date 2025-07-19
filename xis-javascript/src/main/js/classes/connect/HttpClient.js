/**
 * @typedef HttpClient
 * @property {HttpConnector} httpConnector
 * @property {ClientConfig} config
 * @property {string} clientId
 * @property {zoneId}
 */
class HttpClient extends Client {

    /**
     * @param {HttpConnector} httpConnector
     */
    constructor(httpConnector, tokenManager) {
        super(tokenManager);
        this.httpConnector = httpConnector;
        this.resolvedURL = undefined;
    }

    async loadConfig() {
        const response = await this.httpConnector.get('/xis/config', {});
        const config = this.deserializeConfig(response.responseText);
        this.config = config;
        return config;
    }

    async loadPageHead(pageId) {
        const response = await this.httpConnector.get('/xis/page/head', { uri: pageId });
        return response.responseText;
    }

    async loadPageBody(pageId) {
        const response = await this.httpConnector.get('/xis/page/body', { uri: pageId });
        return response.responseText;
    }

    async loadPageBodyAttributes(pageId) {
        const response = await this.httpConnector.get('/xis/page/body-attributes', { uri: pageId });
        return JSON.parse(response.responseText);
    }

    async loadWidget(widgetId) {
        const response = await this.httpConnector.get('/xis/widget/html', { uri: widgetId });
        return response.responseText;
    }


    async handleResponse(response) {
        if (this.serverError(response)) {
            this.handleServerError(response); // TODO use errorhandler
            return Promise.reject();
        }
        if (this.isRedirect(response)) {
            // follow redirect in browser
            return Promise.reject();
        }

        if (this.authorizationRequired(response)) {
            this.forwardToLoginPage();
            return Promise.reject();
        }
        var responseObject = this.deserializeResponse(response);
        if (responseObject.redirectUrl) {
            this.forward(responseObject.redirectUrl);
            return Promise.reject();
        }
        return Promise.resolve(responseObject);
    }

    async loadPageData(resolvedURL) {
        this.resolvedURL = resolvedURL;
        const request = this.createPageRequest(resolvedURL, null, null);
        const response = await this.httpConnector.post('/xis/page/model', request, {});
        return this.handleResponse(response);
    }

    async loadWidgetData(widgetInstance, widgetState) {
        const request = this.createWidgetRequest(widgetInstance, widgetState, null, null, null);
        const response = await this.httpConnector.post('/xis/widget/model', request, {});
        return this.handleResponse(response);
    }

    async loadFormData(resolvedURL, widgetId, formBindingKey, formBindingParameters) {
        const request = this.createFormRequest(resolvedURL, widgetId, {}, null, formBindingKey, formBindingParameters);
        const response = await this.httpConnector.post('/xis/form/model', request, {});
        return this.handleResponse(response);
    }

    async widgetLinkAction(widgetInstance, widgetState, action, actionParameters) {
        const request = this.createWidgetRequest(widgetInstance, widgetState, action, {}, actionParameters);
        const response = await this.httpConnector.post('/xis/widget/action', request, {});
        return this.handleResponse(response);
    }

    async pageLinkAction(resolvedURL, action, actionParameters) {
        const request = this.createPageRequest(resolvedURL, {}, action, actionParameters);
        const response = await this.httpConnector.post('/xis/page/action', request, {});
        return this.handleResponse(response);
    }

    async formAction(resolvedURL, widgetId, formData, action, formBindigKey, formBindingParameters) {
        const request = this.createFormRequest(resolvedURL, widgetId, formData, action, formBindigKey, formBindingParameters);
        const response = await this.httpConnector.post('/xis/form/action', request, {});
        return this.handleResponse(response);
    }


    async sendRenewTokenRequest(renewToken) {
        const response = await this.httpConnector.post('/xis/token/renew', { Authorization: 'Bearer ' + renewToken, renewToken: renewToken }, {});
        return this.deserializeResponse(response);
    }

    forwardToLoginPage() {
        var redirectUri = this.resolvedURL ? this.resolvedURL.toURL() : "/";
        this.forward(this.config.loginPage + '?redirect_uri=' + encodeURIComponent(redirectUri));
    }

    forward(redirectUri) {
        window.location.href = redirectUri;
    }

    authorizationRequired(response) {
        return response.status === 401 || response.status === 403;
    }

    serverError(response) {
        return response.status >= 500 && response.status < 600;
    }

    isRedirect(response) {
        return response.status == 302 || response.status == 303 || response.status == 307 || response.status == 308;
    }

    handleServerError(response) {
        console.error('Server error occurred:', response);
        // Optionally, you can redirect to an error page or show a user-friendly message
    }
}
