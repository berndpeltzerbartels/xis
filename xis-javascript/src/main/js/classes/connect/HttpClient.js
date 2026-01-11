/**
 * @typedef HttpClient
 * @property {HttpConnector} httpConnector
 * @property {ClientConfig} config
 * @property {string} clientId
 * @property {zoneId}
 * @property {Config} config
 */
class HttpClient extends Client {

    /**
     * @param {HttpConnector} httpConnector
     */
    constructor(httpConnector) {
        super();
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
        // pageId kann Umlaute, Leerzeichen, Sonderzeichen enthalten
        // encodeURIComponent ist robust für alle Fälle
        const response = await this.httpConnector.get('/xis/page/head?pageId='+encodeURIComponent(pageId), {});
        return response.responseText;
    }

    async loadPageBody(pageId) {
        const response = await this.httpConnector.get('/xis/page/body?pageId='+encodeURIComponent(pageId), {});
        return response.responseText;
    }

    async loadPageBodyAttributes(pageId) {
        const response = await this.httpConnector.get('/xis/page/body-attributes?pageId='+encodeURIComponent(pageId), {});
        return JSON.parse(response.responseText);
    }

    async loadWidget(widgetId) {
        // widgetId kann ebenfalls Sonderzeichen enthalten
        const response = await this.httpConnector.get('/xis/widget/html?widgetId='+encodeURIComponent(widgetId), {});
        return response.responseText;
    }

    async loadInclude(key) {
        const response = await this.httpConnector.get('/xis/include/html?key='+encodeURIComponent(key), {});
        return response.responseText;
    }


    async loadPageData(resolvedURL) {
        app.messageHandler.clearMessages();
        this.resolvedURL = resolvedURL;
        const request = this.createPageRequest(resolvedURL, null, null);
        try {
            const response = await this.httpConnector.post('/xis/page/model', request, {});
            return this.handleResponse(response);
        } catch (error) {
            reportError('Error during HTTP request to /xis/page/model', error);
            app.messageHandler.reportServerError('connection problem');
            return Promise.reject(error);
        }
    }

    /**
     * @param {WidgetInstance} widgetInstance 
     * @param {WidgetState} widgetState 
     * @returns {Promise<ServerResponse>}
     */
    async loadWidgetData(widgetInstance, widgetState) {
        app.messageHandler.clearMessages();
        const request = this.createWidgetRequest(widgetInstance, widgetState, null, null, null);
        try {
            const response = await this.httpConnector.post('/xis/widget/model', request, {});
            return this.handleResponse(response);
        } catch (error) {
            reportError('Error during HTTP request to /xis/widget/model', error);
              app.messageHandler.reportServerError('connection problem');
            return Promise.reject(error);
        }
    }

    async loadFormData(resolvedURL, widgetId, formBindingKey, widgetParameters) {
        const request = this.createFormRequest(resolvedURL, widgetId, {}, null, formBindingKey, widgetParameters);
        try {
            const response = await this.httpConnector.post('/xis/form/model', request, {});
            return this.handleResponse(response);
        } catch (error) {
            reportError('Error during HTTP request to /xis/form/model', error);
              app.messageHandler.reportServerError('connection problem');
            return Promise.reject(error);
        }
        return this.handleResponse(response);
    }

    async widgetLinkAction(widgetInstance, widgetState, action, actionParameters) {
        app.messageHandler.clearMessages();
        const request = this.createWidgetRequest(widgetInstance, widgetState, action, {}, actionParameters);
        try {
            const response = await this.httpConnector.post('/xis/widget/action', request, {});
            return this.handleResponse(response);
        } catch (error) {
            reportError('Error during HTTP request to /xis/widget/action', error);
              app.messageHandler.reportServerError('connection problem');
            return Promise.reject(error);
        }
    }

    async pageLinkAction(resolvedURL, action, actionParameters) {
        app.messageHandler.clearMessages();
        const request = this.createPageRequest(resolvedURL, {}, action, actionParameters);
        try {
            const response = await this.httpConnector.post('/xis/page/action', request, {});
            return this.handleResponse(response);
        } catch (error) {
            reportError('Error during HTTP request to /xis/page/action', error);
            return Promise.reject(error);
        }

    }

    async formAction(resolvedURL, widgetId, formData, action, formBindigKey, formBindingParameters) {
        app.messageHandler.clearMessages();
        const request = this.createFormRequest(resolvedURL, widgetId, formData, action, formBindigKey, formBindingParameters);
        try {
            const response = await this.httpConnector.post('/xis/form/action', request, {});
            return this.handleResponse(response);
        } catch (error) {
            reportError('Error during HTTP request to /xis/form/action', error);
              app.messageHandler.reportServerError('connection problem');
            return Promise.reject(error);
        }
    }


    async sendRenewTokenRequest(renewToken) {
        try {
            const response = await this.httpConnector.post('/xis/token/renew', { Authorization: 'Bearer ' + renewToken, renewToken: renewToken }, {});
            return this.deserializeResponse(response);
        } catch (error) {
            reportError('Error during HTTP request to /xis/token/renew', error);
            app.messageHandler.reportServerError('connection problem');
            return Promise.reject(error);
        }
    }

    forwardToLoginPage(response) {
        var redirectUri = response.getResponseHeader('Location');
        this.forward(redirectUri);
    }

    forward(redirectUri) {
        window.location.href = redirectUri;
    }

    authorizationRequired(response) {
        return response.status === 401;
    }

    serverError(response) {
        return response.status >= 500 && response.status < 600;
    }

    isAjaxRedirect(response) {
        return response.status == 302 || response.status == 303 || response.status == 307 || response.status == 308;
    }

    doBrowserRedirect(response) {
        var redirectUri = response.getResponseHeader('Location');
        this.forward(redirectUri);
    }

    isBrowserRedirect(response) {
        return !this.isAjaxRedirect(response) && response.getResponseHeader('Location');
    }

    handleServerError(response) {
        console.info('Server error occurred:', response); // do not use reportError(...), here
        return app.messageHandler.reportServerError(JSON.parse(response.responseText).message);
    }

    globalValidatorMessages(response) {
        if (response.validatorMessages && response.validatorMessages.globalMessages) {
            return response.validatorMessages.globalMessages.filter(s => s && s.trim().length > 0);
        }
        return [];
    }
}
