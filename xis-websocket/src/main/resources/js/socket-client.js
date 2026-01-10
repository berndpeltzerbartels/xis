class SocketClient extends Client {
    constructor() {
        super();
        this.ws = null;
        this.messageId = 0;
        this.pendingRequests = new Map();
    }

    connect(url) {
        return new Promise((resolve, reject) => {
            this.ws = new WebSocket(url);
            
            this.ws.onopen = () => {
                console.log('WebSocket connected');
                resolve();
            };
            
            this.ws.onerror = (error) => {
                console.error('WebSocket error:', error);
                reject(error);
            };
            
            this.ws.onmessage = (event) => {
                this.handleMessage(event.data);
            };
            
            this.ws.onclose = () => {
                console.log('WebSocket closed');
            };
        });
    }

    handleMessage(data) {
        const response = JSON.parse(data);
        const pending = this.pendingRequests.get(response.messageId);
        if (pending) {
            this.pendingRequests.delete(response.messageId);
            pending.resolve(response);
        }
    }

    send(url, method, body, headers = {}) {
        return new Promise((resolve, reject) => {
            const messageId = ++this.messageId;
            
            // Extract path and query parameters
            const urlParts = url.split('?');
            const path = urlParts[0];
            const queryParameters = {};
            
            if (urlParts[1]) {
                const params = urlParts[1].split('&');
                for (const param of params) {
                    const [key, value] = param.split('=');
                    queryParameters[decodeURIComponent(key)] = decodeURIComponent(value || '');
                }
            }
            
            const message = {
                messageId,
                path,
                method,
                queryParameters,
                headers,
                body
            };
            
            this.pendingRequests.set(messageId, { resolve, reject });
            this.ws.send(JSON.stringify(message));
        });
    }

    async loadConfig() {
        const response = await this.send('/xis/config', 'GET', null, {});
        const config = this.deserializeConfig(response.responseText);
        this.config = config;
        return config;
    }

    async loadPageHead(pageId) {
        const response = await this.send('/xis/page/head?pageId=' + encodeURIComponent(pageId), 'GET', null, {});
        return response.responseText;
    }

    async loadPageBody(pageId) {
        const response = await this.send('/xis/page/body?pageId=' + encodeURIComponent(pageId), 'GET', null, {});
        return response.responseText;
    }

    async loadPageBodyAttributes(pageId) {
        const response = await this.send('/xis/page/body-attributes?pageId=' + encodeURIComponent(pageId), 'GET', null, {});
        return JSON.parse(response.responseText);
    }

    async loadWidget(widgetId) {
        const response = await this.send('/xis/widget/html?widgetId=' + encodeURIComponent(widgetId), 'GET', null, {});
        return response.responseText;
    }

    async loadInclude(key) {
        const response = await this.send('/xis/include/html?key=' + encodeURIComponent(key), 'GET', null, {});
        return response.responseText;
    }

    async loadPageData(resolvedURL) {
        app.messageHandler.clearMessages();
        this.resolvedURL = resolvedURL;
        const request = this.createPageRequest(resolvedURL, null, null);
        const headers = { 'Content-Type': 'application/json' };
        try {
            const response = await this.send('/xis/page/model', 'POST', request, headers);
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
        const headers = { 'Content-Type': 'application/json' };
        try {
            const response = await this.send('/xis/widget/model', 'POST', request, headers);
            return this.handleResponse(response);
        } catch (error) {
            reportError('Error during WebSocket request to /xis/widget/model', error);
            app.messageHandler.reportServerError('connection problem');
            return Promise.reject(error);
        }
    }

    async loadFormData(resolvedURL, widgetId, formBindingKey, widgetParameters) {
        const request = this.createFormRequest(resolvedURL, widgetId, {}, null, formBindingKey, widgetParameters);
        const headers = { 'Content-Type': 'application/json' };
        try {
            const response = await this.send('/xis/form/model', 'POST', request, headers);
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
        const headers = { 'Content-Type': 'application/json' };
        try {
            const response = await this.send('/xis/widget/action', 'POST', request, headers);
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
        const headers = { 'Content-Type': 'application/json' };
        try {
            const response = await this.send('/xis/page/action', 'POST', request, headers);
            return this.handleResponse(response);
        } catch (error) {
            reportError('Error during WebSocket request to /xis/page/action', error);
            return Promise.reject(error);
        }
    }

    async formAction(resolvedURL, widgetId, formData, action, formBindigKey, formBindingParameters) {
        app.messageHandler.clearMessages();
        const request = this.createFormRequest(resolvedURL, widgetId, formData, action, formBindigKey, formBindingParameters);
        const headers = { 'Content-Type': 'application/json' };
        try {
            const response = await this.send('/xis/form/action', 'POST', request, headers);
            return this.handleResponse(response);
        } catch (error) {
            reportError('Error during WebSocket request to /xis/form/action', error);
            app.messageHandler.reportServerError('connection problem');
            return Promise.reject(error);
        }
    }

    async sendRenewTokenRequest(renewToken) {
        const headers = { 
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + renewToken 
        };
        try {
            const response = await this.send('/xis/token/renew', 'POST', { renewToken }, headers);
            return this.deserializeResponse(response);
        } catch (error) {
            reportError('Error during WebSocket request to /xis/token/renew', error);
            app.messageHandler.reportServerError('connection problem');
            return Promise.reject(error);
        }
    }

    async handleResponse(response) {
        // Check for errors using WebSocket-specific fields
        if (!response.success) {
            if (response.errorCode === 'SERVER_ERROR') {
                this.handleServerError(response);
                return Promise.reject();
            }
            if (response.errorCode === 'UNAUTHORIZED') {
                this.forwardToLoginPage(response);
                return Promise.reject({ type: 'redirect' });
            }
            if (response.redirectUrl) {
                this.forward(response.redirectUrl);
                return Promise.reject({ type: 'redirect' });
            }
        }
        
        var responseObject = this.deserializeResponse(response);
        if (responseObject.redirectUrl) {
            this.forward(responseObject.redirectUrl);
            return Promise.reject({ type: 'redirect' });
        }
        const globalMessages = this.globalValidatorMessges(responseObject);
        if (globalMessages.length > 0) {
            app.messageHandler.addValidationErrors(globalMessages);
        }
        return Promise.resolve(responseObject);
    }

    forwardToLoginPage(response) {
        var redirectUri = response.redirectUrl || '/login';
        this.forward(redirectUri);
    }

    forward(redirectUri) {
        window.location.href = redirectUri;
    }

    handleServerError(response) {
        console.info('Server error occurred:', response);
        const errorMessage = response.errorMessage || response.message || 'Unknown server error';
        return app.messageHandler.reportServerError(errorMessage);
    }

    globalValidatorMessges(response) {
        if (response.validatorMessages && response.validatorMessages.globalMessages) {
            return response.validatorMessages.globalMessages.filter(s => s && s.trim().length > 0);
        }
        return [];
    }
}