class WebsocketClient extends Client {
    
    /**
     * @param {WebsocketConnector} wsConnector
     */
    constructor(wsConnector, clientId) {
        super(clientId);
        this.wsConnector = wsConnector;
        this.clientId = clientId;
        this.resolvedURL = undefined;
    }

    /**
     * Reads a cookie value by name.
     * Returns null if not present (no token = anonymous/no security configured).
     * @private
     */
    getTokenFromCookie(name) {
        const match = document.cookie && document.cookie.split(';')
            .map(c => c.trim())
            .find(c => c.startsWith(name + '='));
        return match ? decodeURIComponent(match.substring(name.length + 1)) : null;
    }

    /**
     * Writes a cookie. maxAge in seconds.
     * @private
     */
    setTokenCookie(name, value, maxAge) {
        let cookie = name + '=' + encodeURIComponent(value) + '; path=/; SameSite=Strict';
        if (maxAge != null) {
            cookie += '; Max-Age=' + maxAge;
        }
        document.cookie = cookie;
    }

    /**
     * Appends accessToken and renewToken from cookies to the request body.
     * No-op if cookies are absent (security not configured).
     * @private
     */
    applyTokens(request) {
        const accessToken = this.getTokenFromCookie('access_token');
        const renewToken = this.getTokenFromCookie('renew_token');
        if (accessToken) request.accessToken = accessToken;
        if (renewToken) request.renewToken = renewToken;
        return request;
    }

    /**
     * If the server renewed the tokens, update the cookies.
     * @private
     */
    handleRenewedTokens(response) {
        if (!response || !response.headers) return;
        const newAccessToken = response.headers['X-Access-Token'];
        const newRenewToken  = response.headers['X-Renew-Token'];
        const expiresIn      = response.headers['X-Token-Expires-In'];
        const renewExpiresIn = response.headers['X-Renew-Token-Expires-In'];
        if (newAccessToken) {
            this.setTokenCookie('access_token', newAccessToken, expiresIn ? parseInt(expiresIn) : null);
        }
        if (newRenewToken) {
            this.setTokenCookie('renew_token', newRenewToken, renewExpiresIn ? parseInt(renewExpiresIn) : null);
        }
    }

    setConfig(config) {
        return new Promise((resolve, _) => {
            this.config = config;
            resolve(config);
        });
    }

    async loadPageData(resolvedURL) {
        app.messageHandler.clearMessages();
        this.resolvedURL = resolvedURL;
        const request = this.applyTokens(this.createPageRequest(resolvedURL, null, null));
        console.debug("loading page data");
        const response = await this.wsConnector.send('/xis/page/model', 'POST', request, {});
        this.handleRenewedTokens(response);
        return this.handleResponse(response);
    }

    async loadWidgetData(widgetInstance, widgetState) {
        app.messageHandler.clearMessages();
        const request = this.applyTokens(this.createWidgetRequest(widgetInstance, widgetState, null, null, null));
        console.debug("loading widget data");
        const response = await this.wsConnector.send('/xis/widget/model', 'POST', request, {});
        this.handleRenewedTokens(response);
        return this.handleResponse(response);
    }

    async loadFormData(resolvedURL, widgetId, formBindingKey, widgetParameters) {
        const request = this.applyTokens(this.createFormRequest(resolvedURL, widgetId, {}, null, formBindingKey, widgetParameters));
        console.debug("loading form data");
        const response = await this.wsConnector.send('/xis/form/model', 'POST', request, {});
        this.handleRenewedTokens(response);
        return this.handleResponse(response);
    }

    async widgetLinkAction(widgetInstance, widgetState, action, actionParameters) {
        app.messageHandler.clearMessages();
        const request = this.applyTokens(this.createWidgetRequest(widgetInstance, widgetState, action, {}, actionParameters));
        console.debug("submitting link-action");
        const response = await this.wsConnector.send('/xis/widget/action', 'POST', request, {});
        this.handleRenewedTokens(response);
        return this.handleResponse(response);
    }

    async pageLinkAction(resolvedURL, action, actionParameters) {
        app.messageHandler.clearMessages();
        const request = this.applyTokens(this.createPageRequest(resolvedURL, {}, action, actionParameters));
        console.debug("submitting link-action");
        const response = await this.wsConnector.send('/xis/page/action', 'POST', request, {});
        this.handleRenewedTokens(response);
        return this.handleResponse(response);
    }

    async formAction(resolvedURL, widgetId, formData, action, formBindigKey, formBindingParameters) {
        app.messageHandler.clearMessages();
        const request = this.applyTokens(this.createFormRequest(resolvedURL, widgetId, formData, action, formBindigKey, formBindingParameters));
        console.debug("submitting form-action");
        const response = await this.wsConnector.send('/xis/form/action', 'POST', request, {});
        this.handleRenewedTokens(response);
        return this.handleResponse(response);
    }

    async sendRenewTokenRequest(renewToken) {
        const response = await this.wsConnector.send('/xis/token/renew', 'POST', {}, {});
        this.handleRenewedTokens(response);
        return this.deserializeResponse(response);
    }

    async handleResponse(response) {
        console.debug("handle response");
        if (this.serverError(response)) {
            console.error("server error");
            this.handleServerError(response);
            return Promise.reject();
        }
        if (this.isAjaxRedirect(response)) {
            console.debug("redirect");
            return Promise.reject({type: 'redirect'});
        }
        if (this.authorizationRequired(response)) {
            this.forwardToLoginPage(response);
            return Promise.reject({type: 'redirect'});
        }
        if (this.isBrowserRedirect(response)) {
            this.doBrowserRedirect(response);
            return Promise.reject({type: 'redirect'});
        }
        var responseObject = this.deserializeResponse(response);
        if (responseObject.redirectUrl) {
            console.debug("redirect in deserialized response");
            this.forward(responseObject.redirectUrl);
            return Promise.reject({type: 'redirect'});
        }
        const globalMessages = this.globalValidatorMessages(responseObject);
        if (globalMessages.length > 0) {
            console.debug("place global validator messages");
            app.messageHandler.addValidationErrors(globalMessages);
        }
        console.debug("delegating response");
        return Promise.resolve(responseObject);
    }
}

