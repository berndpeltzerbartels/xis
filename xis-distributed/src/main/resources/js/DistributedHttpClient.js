/**
 * xis-distributed – HTTP routing extension
 *
 * When xis-distributed is on the classpath this script is loaded as a JS
 * extension (META-INF/xis/js/extensions).  It patches HttpClient so that
 * requests for remote pages and widgets are sent to the correct host instead
 * of the local server.
 *
 * The host information is already present in ClientConfig (pageAttributes[id].host
 * and widgetAttributes[id].host) – this file just wires it up.
 *
 * No changes are required to the application or to any other XIS JS class.
 */

(function () {

    /**
     * Resolves a base URL for a page request.
     * Returns an empty string for local pages (host not configured).
     * @param {string} normalizedPath
     * @returns {string}
     */
    function pageBaseUrl(normalizedPath) {
        if (!app || !app.client || !app.client.config) return '';
        const host = app.client.config.getPageHost(normalizedPath);
        return host || '';
    }

    /**
     * Resolves a base URL for a widget request.
     * Returns an empty string for local widgets (host not configured).
     * @param {string} widgetId
     * @returns {string}
     */
    function widgetBaseUrl(widgetId) {
        if (!app || !app.client || !app.client.config) return '';
        const host = app.client.config.getWidgetHost(widgetId);
        return host || '';
    }

    // -------------------------------------------------------------------------
    // Patch HttpClient – only the methods that carry a component-specific host
    // -------------------------------------------------------------------------

    const _origLoadPageHead = HttpClient.prototype.loadPageHead;
    HttpClient.prototype.loadPageHead = async function (pageId) {
        const base = pageBaseUrl(pageId);
        if (!base) return _origLoadPageHead.call(this, pageId);
        const response = await this.httpConnector.get(base + '/xis/page/head?pageId=' + encodeURIComponent(pageId), {});
        return response.responseText;
    };

    const _origLoadPageBody = HttpClient.prototype.loadPageBody;
    HttpClient.prototype.loadPageBody = async function (pageId) {
        const base = pageBaseUrl(pageId);
        if (!base) return _origLoadPageBody.call(this, pageId);
        const response = await this.httpConnector.get(base + '/xis/page/body?pageId=' + encodeURIComponent(pageId), {});
        return response.responseText;
    };

    const _origLoadPageBodyAttributes = HttpClient.prototype.loadPageBodyAttributes;
    HttpClient.prototype.loadPageBodyAttributes = async function (pageId) {
        const base = pageBaseUrl(pageId);
        if (!base) return _origLoadPageBodyAttributes.call(this, pageId);
        const response = await this.httpConnector.get(base + '/xis/page/body-attributes?pageId=' + encodeURIComponent(pageId), {});
        return JSON.parse(response.responseText);
    };

    const _origLoadPageData = HttpClient.prototype.loadPageData;
    HttpClient.prototype.loadPageData = async function (resolvedURL) {
        const base = pageBaseUrl(resolvedURL.normalizedPath);
        if (!base) return _origLoadPageData.call(this, resolvedURL);
        app.messageHandler.clearMessages();
        this.resolvedURL = resolvedURL;
        const request = this.createPageRequest(resolvedURL, null, null);
        try {
            const response = await this.httpConnector.post(base + '/xis/page/model', request, {});
            return this.handleResponse(response);
        } catch (error) {
            reportError('Error during distributed HTTP request to /xis/page/model', error);
            app.messageHandler.reportServerError('connection problem');
            return Promise.reject(error);
        }
    };

    const _origPageLinkAction = HttpClient.prototype.pageLinkAction;
    HttpClient.prototype.pageLinkAction = async function (resolvedURL, action, actionParameters) {
        const base = pageBaseUrl(resolvedURL.normalizedPath);
        if (!base) return _origPageLinkAction.call(this, resolvedURL, action, actionParameters);
        app.messageHandler.clearMessages();
        const request = this.createPageRequest(resolvedURL, {}, action, actionParameters);
        try {
            const response = await this.httpConnector.post(base + '/xis/page/action', request, {});
            return this.handleResponse(response);
        } catch (error) {
            reportError('Error during distributed HTTP request to /xis/page/action', error);
            return Promise.reject(error);
        }
    };

    const _origLoadWidget = HttpClient.prototype.loadWidget;
    HttpClient.prototype.loadWidget = async function (widgetId) {
        const base = widgetBaseUrl(widgetId);
        if (!base) return _origLoadWidget.call(this, widgetId);
        const response = await this.httpConnector.get(base + '/xis/widget/html?widgetId=' + encodeURIComponent(widgetId), {});
        return response.responseText;
    };

    const _origLoadWidgetData = HttpClient.prototype.loadWidgetData;
    HttpClient.prototype.loadWidgetData = async function (widgetInstance, widgetState) {
        const base = widgetBaseUrl(widgetInstance.widgetId);
        if (!base) return _origLoadWidgetData.call(this, widgetInstance, widgetState);
        app.messageHandler.clearMessages();
        const request = this.createWidgetRequest(widgetInstance, widgetState, null, null, null);
        try {
            const response = await this.httpConnector.post(base + '/xis/widget/model', request, {});
            return this.handleResponse(response);
        } catch (error) {
            reportError('Error during distributed HTTP request to /xis/widget/model', error);
            app.messageHandler.reportServerError('connection problem');
            return Promise.reject(error);
        }
    };

    const _origWidgetLinkAction = HttpClient.prototype.widgetLinkAction;
    HttpClient.prototype.widgetLinkAction = async function (widgetInstance, widgetState, action, actionParameters) {
        const base = widgetBaseUrl(widgetInstance.widgetId);
        if (!base) return _origWidgetLinkAction.call(this, widgetInstance, widgetState, action, actionParameters);
        app.messageHandler.clearMessages();
        const request = this.createWidgetRequest(widgetInstance, widgetState, action, {}, actionParameters);
        try {
            const response = await this.httpConnector.post(base + '/xis/widget/action', request, {});
            return this.handleResponse(response);
        } catch (error) {
            reportError('Error during distributed HTTP request to /xis/widget/action', error);
            app.messageHandler.reportServerError('connection problem');
            return Promise.reject(error);
        }
    };

    const _origLoadFormData = HttpClient.prototype.loadFormData;
    HttpClient.prototype.loadFormData = async function (resolvedURL, widgetId, formBindingKey, widgetParameters) {
        const base = widgetBaseUrl(widgetId);
        if (!base) return _origLoadFormData.call(this, resolvedURL, widgetId, formBindingKey, widgetParameters);
        const request = this.createFormRequest(resolvedURL, widgetId, {}, null, formBindingKey, widgetParameters);
        try {
            const response = await this.httpConnector.post(base + '/xis/form/model', request, {});
            return this.handleResponse(response);
        } catch (error) {
            reportError('Error during distributed HTTP request to /xis/form/model', error);
            app.messageHandler.reportServerError('connection problem');
            return Promise.reject(error);
        }
    };

    const _origFormAction = HttpClient.prototype.formAction;
    HttpClient.prototype.formAction = async function (resolvedURL, widgetId, formData, action, formBindingKey, formBindingParameters) {
        const base = widgetBaseUrl(widgetId);
        if (!base) return _origFormAction.call(this, resolvedURL, widgetId, formData, action, formBindingKey, formBindingParameters);
        app.messageHandler.clearMessages();
        const request = this.createFormRequest(resolvedURL, widgetId, formData, action, formBindingKey, formBindingParameters);
        try {
            const response = await this.httpConnector.post(base + '/xis/form/action', request, {});
            return this.handleResponse(response);
        } catch (error) {
            reportError('Error during distributed HTTP request to /xis/form/action', error);
            app.messageHandler.reportServerError('connection problem');
            return Promise.reject(error);
        }
    };

})();
