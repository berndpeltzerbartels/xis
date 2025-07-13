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
        const headers = await this.authenticationHeader();
        headers.uri = widgetId;
        const response = await this.httpConnector.get('/xis/widget/html', headers);
        return response.responseText;
    }

    async loadPageData(resolvedURL, queryParameters) {
        const request = this.createPageRequest(resolvedURL, null, null);
        const headers = await this.authenticationHeader();
        const response = await this.httpConnector.post('/xis/page/model', request, headers);
        if (this.handleRedirect(response)) {
            return Promise.reject();
        }
        return this.deserializeResponse(response);
    }

    async loadWidgetData(widgetInstance, widgetState) {
        const request = this.createWidgetRequest(widgetInstance, widgetState, null, null, null);
        const headers = await this.authenticationHeader();
        const response = await this.httpConnector.post('/xis/widget/model', request, headers);
        if (this.handleRedirect(response)) {
            return Promise.reject();
        }
        return this.deserializeResponse(response);
    }

    async loadFormData(resolvedURL, widgetId, formBindingKey, formBindingParameters) {
        const request = this.createFormRequest(resolvedURL, widgetId, {}, null, formBindingKey, formBindingParameters);
        const headers = await this.authenticationHeader();
        const response = await this.httpConnector.post('/xis/form/model', request, headers);
        if (this.handleRedirect(response)) {
            return Promise.reject();
        }
        return this.deserializeResponse(response);
    }

    async widgetLinkAction(widgetInstance, widgetState, action, actionParameters) {
        const request = this.createWidgetRequest(widgetInstance, widgetState, action, {}, actionParameters);
        const headers = await this.authenticationHeader();
        const response = await this.httpConnector.post('/xis/widget/action', request, headers);
        if (this.handleRedirect(response)) {
            return Promise.reject();
        }
        return this.deserializeResponse(response);
    }

    async pageLinkAction(resolvedURL, action, actionParameters) {
        const request = this.createPageRequest(resolvedURL, {}, action, actionParameters);
        const headers = await this.authenticationHeader();
        const response = await this.httpConnector.post('/xis/page/action', request, headers);
        if (this.handleRedirect(response)) {
            return Promise.reject();
        }
        return this.deserializeResponse(response);
    }

    async formAction(resolvedURL, widgetId, formData, action, formBindigKey, formBindingParameters) {
        const request = this.createFormRequest(resolvedURL, widgetId, formData, action, formBindigKey, formBindingParameters);
        const headers = await this.authenticationHeader();
        const response = await this.httpConnector.post('/xis/form/action', request, headers);
        debugger;
        const cookies = this.readCookies(response);
        if (response.getResponseHeader('X-Access-Token')) {
            this.tokenManager.setAccessToken(response.getResponseHeader('X-Access-Token'));
        }
        if (this.handleRedirect(response)) {
            return Promise.reject();
        }
        return this.deserializeResponse(response);
    }


    handleRedirect(response) {
        const location = response.getResponseHeader('X-Redirect-Location');
        if (location) {
            app.pageController.displayPageForUrl(location);
            return true;
        }
        return false;
    }

    async sendRenewTokenRequest(renewToken) {
        const response = await this.httpConnector.post('/xis/token/renew', { Authorization: 'Bearer ' + renewToken, renewToken: renewToken }, {});
        return this.deserializeResponse(response);
    }

    /**
     * @private
     * Returns the authentication header for the current user.
     * This header is used for all requests that require authentication.
     * 
     * @returns {Promise<Object>} The authentication header with the access token.
     */
    async authenticationHeader() {
        debugger;
        const token = await this.tokenManager.actualAccessToken();
        const header = {};
        if (token) {
            header['Authorization'] = 'Bearer ' + token;
        }
        return header;
    }

    /**
   * Liest alle Cookies aus dem Set-Cookie-Header der Response und gibt ein Objekt mit Key-Value-Paaren zurück.
   * @param {XMLHttpRequest} response - Die XMLHttpRequest-Response
   * @returns {Object} - Objekt mit Cookie-Namen als Keys und deren Werte als Values
   */
    readCookies(response) {
        const allHeaders = response.getAllResponseHeaders();
        const setCookieHeaders = allHeaders
            .split('\r\n')
            .filter(header => header.toLowerCase().startsWith('set-cookie:'));

        const cookieObj = {};
        for (const header of setCookieHeaders) {
            const cookieString = header.substring('set-cookie:'.length).trim();
            const [name, ...rest] = cookieString.split('=');
            if (name && rest.length > 0) {
                cookieObj[name] = rest.join('=').split(';')[0]; // Nur den Wert vor dem ersten Semikolon nehmen
            }
        }
        return cookieObj;
    }
}
