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
     * @param {string} clientId
     */
    constructor(httpConnector, clientId) {
        super(clientId);
        this.httpConnector = httpConnector;
        this.clientId = clientId;
        this.resolvedURL = undefined;
    }

    async loadConfig() {
        const response = await this.httpConnector.get('/xis/config', {});
        const config = this.deserializeConfig(response.responseText);
        await this.loadDistributedConfigs(config);
        this.config = config;
        return config;
    }

    async loadDistributedConfigs(config) {
        const hosts = await this.loadDistributedHosts();
        if (!hosts || hosts.length === 0) {
            return config;
        }
        for (const host of hosts) {
            const response = await this.httpConnector.get(this.resolveUri(host, '/xis/config'), {});
            const remoteConfig = this.deserializeConfig(response.responseText);
            this.mergeRemoteConfig(config, remoteConfig, host);
        }
        return config;
    }

    async loadDistributedHosts() {
        try {
            const response = await this.httpConnector.get('/xis/distributed/hosts', {});
            if (response.status === 404) {
                return [];
            }
            return response.responseText ? JSON.parse(response.responseText) : [];
        } catch (error) {
            if (error && error.status === 404) {
                return [];
            }
            throw error;
        }
    }

    mergeRemoteConfig(config, remoteConfig, host) {
        for (const pageId of remoteConfig.pageIds || []) {
            config.pageIds.push(pageId);
        }
        for (const frontletId of remoteConfig.frontletIds || []) {
            config.frontletIds.push(frontletId);
        }
        for (const includeId of remoteConfig.includeIds || []) {
            config.includeIds.push(includeId);
        }
        for (const pageId of Object.keys(remoteConfig.pageAttributes || {})) {
            remoteConfig.pageAttributes[pageId].host = host;
            config.pageAttributes[pageId] = remoteConfig.pageAttributes[pageId];
        }
        for (const frontletId of Object.keys(remoteConfig.frontletAttributes || {})) {
            remoteConfig.frontletAttributes[frontletId].host = host;
            config.frontletAttributes[frontletId] = remoteConfig.frontletAttributes[frontletId];
        }
    }

    async loadMessages(locale) {
        const uri = locale ? '/xis/messages?locale=' + encodeURIComponent(locale) : '/xis/messages';
        const response = await this.httpConnector.get(uri, {});
        app.messages = response.responseText ? JSON.parse(response.responseText) : {};
        return app.messages;
    }

    async loadPageHead(pageId) {
        // pageId kann Umlaute, Leerzeichen, Sonderzeichen enthalten
        // encodeURIComponent ist robust für alle Fälle
        const response = await this.httpConnector.get(this.resolvePageUri('/xis/page/head?pageId='+encodeURIComponent(pageId), pageId), {});
        return response.responseText;
    }

    async loadPageBody(pageId) {
        const response = await this.httpConnector.get(this.resolvePageUri('/xis/page/body?pageId='+encodeURIComponent(pageId), pageId), {});
        return response.responseText;
    }

    async loadPageBodyAttributes(pageId) {
        const response = await this.httpConnector.get(this.resolvePageUri('/xis/page/body-attributes?pageId='+encodeURIComponent(pageId), pageId), {});
        return JSON.parse(response.responseText);
    }

    async loadFrontlet(frontletId) {
        // frontletId kann ebenfalls Sonderzeichen enthalten
        const response = await this.httpConnector.get(this.resolveFrontletUri('/xis/frontlet/html?frontletId='+encodeURIComponent(frontletId), frontletId), {});
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
            const response = await this.httpConnector.post(this.resolvePageUri('/xis/page/model', resolvedURL.normalizedPath), request, {});
            return this.handleResponse(response);
        } catch (error) {
            return this.handleRequestError('Error during HTTP request to /xis/page/model', error);
        }
    }

    /**
     * @param {FrontletInstance} frontletInstance
     * @param {FrontletState} frontletState
     * @returns {Promise<ServerResponse>}
     */
    async loadFrontletData(frontletInstance, frontletState) {
        app.messageHandler.clearMessages();
        const request = this.createFrontletRequest(frontletInstance, frontletState, null, null, null);
        try {
            const response = await this.httpConnector.post(this.resolveFrontletUri('/xis/frontlet/model', frontletInstance.frontlet.id), request, {});
            return this.handleResponse(response);
        } catch (error) {
            return this.handleRequestError('Error during HTTP request to /xis/frontlet/model', error);
        }
    }

    async loadFormData(resolvedURL, frontletId, formBindingKey, frontletParameters, load) {
        const request = this.createFormRequest(resolvedURL, frontletId, {}, null, formBindingKey, frontletParameters, load);
        try {
            const response = await this.httpConnector.post(this.resolveFormUri('/xis/form/model', resolvedURL, frontletId), request, {});
            return this.handleResponse(response);
        } catch (error) {
            return this.handleRequestError('Error during HTTP request to /xis/form/model', error);
        }
        return this.handleResponse(response);
    }

    async frontletLinkAction(frontletInstance, frontletState, action, actionParameters) {
        app.messageHandler.clearMessages();
        const request = this.createFrontletRequest(frontletInstance, frontletState, action, {}, actionParameters);
        try {
            const response = await this.httpConnector.post(this.resolveFrontletUri('/xis/frontlet/action', frontletInstance.frontlet.id), request, {});
            return this.handleResponse(response);
        } catch (error) {
            return this.handleRequestError('Error during HTTP request to /xis/frontlet/action', error);
        }
    }

    async pageLinkAction(resolvedURL, action, actionParameters) {
        app.messageHandler.clearMessages();
        const request = this.createPageRequest(resolvedURL, {}, action, actionParameters);
        try {
            const response = await this.httpConnector.post(this.resolvePageUri('/xis/page/action', resolvedURL.normalizedPath), request, {});
            return this.handleResponse(response);
        } catch (error) {
            return this.handleRequestError('Error during HTTP request to /xis/page/action', error, false);
        }

    }

    async formAction(resolvedURL, frontletId, formData, action, formBindigKey, formBindingParameters, uploads) {
        app.messageHandler.clearMessages();
        const request = this.createFormRequest(resolvedURL, frontletId, formData, action, formBindigKey, formBindingParameters);
        try {
            const response = await this.httpConnector.post(this.resolveFormUri('/xis/form/action', resolvedURL, frontletId), request, {}, uploads);
            return this.handleResponse(response);
        } catch (error) {
            return this.handleRequestError('Error during HTTP request to /xis/form/action', error);
        }
    }


    async sendRenewTokenRequest(renewToken) {
        try {
            const response = await this.httpConnector.post('/xis/token/renew', { Authorization: 'Bearer ' + renewToken, renewToken: renewToken }, {});
            return this.deserializeResponse(response);
        } catch (error) {
            return this.handleRequestError('Error during HTTP request to /xis/token/renew', error);
        }
    }

    handleRequestError(message, error, reportConnectionProblem = true) {
        if (error && error.type === 'redirect') {
            return Promise.reject(error);
        }
        reportError(message, error);
        if (reportConnectionProblem) {
            app.messageHandler.reportServerError('connection problem');
        }
        return Promise.reject(error);
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
        console.error('Server error occurred:', response); // do not use reportError(...), here
        return app.messageHandler.reportServerError(this.readServerErrorMessage(response));
    }

    readServerErrorMessage(response) {
        if (!response || !response.responseText) {
            return 'Internal server error';
        }
        try {
            var parsed = JSON.parse(response.responseText);
            return parsed.message || response.responseText;
        } catch (e) {
            return response.responseText;
        }
    }

    globalValidatorMessages(response) {
        const globalMessages = response.validatorMessages ? response.validatorMessages.globalMessages : [];
        if (Array.isArray(globalMessages)) {
            return globalMessages.filter(s => s && s.trim().length > 0);
        }
        return [];
    }

    resolvePageUri(path, normalizedPath) {
        return this.resolveUri(this.config.getPageHost(normalizedPath), path);
    }

    resolveFrontletUri(path, frontletId) {
        return this.resolveUri(this.config.getFrontletHost(frontletId), path);
    }

    resolveFormUri(path, resolvedURL, frontletId) {
        if (frontletId) {
            return this.resolveFrontletUri(path, frontletId);
        }
        return this.resolvePageUri(path, resolvedURL.normalizedPath);
    }

    resolveUri(host, path) {
        if (!host) {
            return path;
        }
        if (host.endsWith('/') && path.startsWith('/')) {
            return host.substring(0, host.length - 1) + path;
        }
        if (!host.endsWith('/') && !path.startsWith('/')) {
            return host + '/' + path;
        }
        return host + path;
    }
}
