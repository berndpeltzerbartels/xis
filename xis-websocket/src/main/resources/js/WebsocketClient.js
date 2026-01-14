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


    setConfig(config) {
        return new Promise((resolve, _) => {
            this.config = config;
            resolve(config);
        });
    }

    async loadPageData(resolvedURL) {
        app.messageHandler.clearMessages();
        this.resolvedURL = resolvedURL;
        const request = this.createPageRequest(resolvedURL, null, null);
        try {
            console.debug("loading page data");
            const response = await this.wsConnector.send('/xis/page/model', 'POST', request, {});
            return this.handleResponse(response);
        } catch (error) {
            reportError('Error during WebSocket request to /xis/page/model', error);
            app.messageHandler.reportServerError('connection problem');
            return Promise.reject(error);
        }
    }

    async loadWidgetData(widgetInstance, widgetState) {
        app.messageHandler.clearMessages();
        const request = this.createWidgetRequest(widgetInstance, widgetState, null, null, null);
        try {
            console.debug("loading widget data");
            const response = await this.wsConnector.send('/xis/widget/model', 'POST', request, {});
            return this.handleResponse(response);
        } catch (error) {
            reportError('Error during WebSocket request to /xis/widget/model', error);
            app.messageHandler.reportServerError('connection problem');
            return Promise.reject(error);
        }
    }

    async loadFormData(resolvedURL, widgetId, formBindingKey, widgetParameters) {
        const request = this.createFormRequest(resolvedURL, widgetId, {}, null, formBindingKey, widgetParameters);
        try {
            console.debug("loading form data");
            const response = await this.wsConnector.send('/xis/form/model', 'POST', request, {});
            return this.handleResponse(response);
        } catch (error) {
            reportError('Error during WebSocket request to /xis/form/model', error);
            app.messageHandler.reportServerError('connection problem');
            return Promise.reject(error);
        }
    }

    async widgetLinkAction(widgetInstance, widgetState, action, actionParameters) {
        app.messageHandler.clearMessages();
        const request = this.createWidgetRequest(widgetInstance, widgetState, action, {}, actionParameters);
        try {
            console.debug("submitting link-action");
            const response = await this.wsConnector.send('/xis/widget/action', 'POST', request, {});
            return this.handleResponse(response);
        } catch (error) {
            reportError('Error during WebSocket request to /xis/widget/action', error);
            app.messageHandler.reportServerError('connection problem');
            return Promise.reject(error);
        }
    }

    async pageLinkAction(resolvedURL, action, actionParameters) {
        app.messageHandler.clearMessages();
        const request = this.createPageRequest(resolvedURL, {}, action, actionParameters);
        try {
           console.debug("submitting link-action");
            const response = await this.wsConnector.send('/xis/page/action', 'POST', request, {});
            return this.handleResponse(response);
        } catch (error) {
            reportError('Error during WebSocket request to /xis/page/action', error);
            return Promise.reject(error);
        }
    }

    async formAction(resolvedURL, widgetId, formData, action, formBindigKey, formBindingParameters) {
        app.messageHandler.clearMessages();
        const request = this.createFormRequest(resolvedURL, widgetId, formData, action, formBindigKey, formBindingParameters);
        try {
           console.debug("submitting form-action");
            const response = await this.wsConnector.send('/xis/form/action', 'POST', request, {});
            return this.handleResponse(response);
        } catch (error) {
            reportError('Error during WebSocket request to /xis/form/action', error);
            app.messageHandler.reportServerError('connection problem');
            return Promise.reject(error);
        }
    }

    async sendRenewTokenRequest(renewToken) {
        try {
            const response = await this.wsConnector.send('/xis/token/renew', 'POST', {}, {});
            return this.deserializeResponse(response);
        } catch (error) {
            reportError('Error during WebSocket request to /xis/token/renew', error);
            app.messageHandler.reportServerError('connection problem');
            return Promise.reject(error);
        }
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
