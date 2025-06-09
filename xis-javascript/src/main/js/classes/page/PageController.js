/**
 * 
 * @typedef PageController
 * @property {HttpClient} client
 * @property {Pages} pages
 * @property {Initializer} initializer
 * @property {URLResolver} urlResolver
 * @property {Page} page
 * @property {HtmlTagHandler} html
 * @property {ClientConfig} config
 * @property {ResolvedURL} resolvedURL
 * 
 * A singleton responsible for placing or replacing
 * subelements of the root-page, our basic html document 
 * never get replaced.
 */
class PageController {

    /**
     * @param {HttpClient} client
     * @param {Pages} pages
     * @param {Initializer} initializer
     * @param {URLResolver} urlResolver
     * @param {TagHandlers} tagHandlers
     */
    constructor(client, pages, initializer, urlResolver, tagHandlers) {
        this.client = client;
        this.pages = pages;
        this.initializer = initializer;
        this.urlResolver = urlResolver;
        this.htmlTagHandler = new HtmlTagHandler(tagHandlers);
        this.page = undefined;
        this.resolvedURL = undefined;
        this.config = undefined;
    }

    /**
     * Should be used in case of the source 
     * of the action has no parent widget-container
     * and invoker has no data.
     * 
     * @see ActionLinkHandler
     * 
     * @public
     * @param {String} action
     * @param {Object} actionParameters
     * @returns {Promise<void>}
     */
    submitPageLinkAction(action, actionParameters) {
        return this.client.pageLinkAction(this.resolvedURL, action, actionParameters)
            .then(response => this.handleActionResponse(response));
    }

    /**
   * Should be used in case of the source 
   * of the action has no parent widget-container
   * and invoker has data.
   * 
   * @see FormHandler
   * 
   * @public
   * @param {String} action
   * @param {Data} formData
   * @returns {Promise<void>}
   */
    submitFormAction(action, formData) {
        return this.client.pageAction(this.resolvedURL, formData, action, {})
            .then(response => this.handleActionResponse(response));
    }

    /**
     * Handels server-response after submitting an action.
     * 
     * @public
     * @param {ServerResponse} response
     */
    handleActionResponse(response) {
        this.handleActionResponseNoContent(response);
        if (response.status == 204) {
            return;
        }
        var data = response.data;
        data.scope = 'TREE';
        this.doRefresh(data);
    }

    triggerPageReload(response) {
        var data = response.data;
        data.scope = 'CONTROLLER';
        this.doRefresh(response);
    }

    doRefresh(data) {
        data.setValue(['pathVariables'], this.resolvedURL.pathVariablesAsMap());
        data.setValue(['urlParameters'], this.resolvedURL.urlParameters);
        this.page.data = data;
        this.htmlTagHandler.refresh(this.page.data);
        this.updateHistory(this.resolvedURL);
    }

    triggerAdditionalReloads(response) {
        app.backendService.triggerWidgetReloadsonDemand(response);
        return response;
    }


    /** 
     * Handels server-response after submitting an action.
     * 
     * @public
     * @param {ServerResponse} response
     */
    handleActionResponseNoContent(response) {
        if (response.nextPageURL) {
            var resolvedURL = this.urlResolver.resolve(response.nextPageURL);
            if (!resolvedURL) {
                throw new Error('no page for ' + response.nextPageURL);
            }
            this.resolvedURL = resolvedURL;
            if (resolvedURL.page != this.page) {
                this.page = resolvedURL.page;
                this.htmlTagHandler.unbindPage();
                this.htmlTagHandler.bindPage(resolvedURL.page);
            }
        }
        this.updateHistory(this.resolvedURL);
    }


    /**
     * Handels server-response after submitting an action.
     *  @public
     * @param {ServerResponse} response
     * @returns {Promise<void>}
     *  
     * */
    handleActionResponseUnprocessableEntity(response) {

    }

    getData() {
        return this.page ? this.page.data : undefined;
    }

    /**
    * Displays a page from a given URL.
    * Optionally skips browser history update (used e.g. for popstate navigation).
    * 
    * @param {string} realUrl - The URL to resolve and load the page for.
    * @param {boolean} [skipHistoryUpdate=false] - If true, skips adding to browser history.
    */
    displayPageForUrl(realUrl, skipHistoryUpdate = false) {
        debugger;
        const resolved = this.urlResolver.resolve(realUrl) || this.welcomePageUrl();
        if (!resolved) {
            throw new Error('No page found for URL: ' + realUrl);
        }

        if (!this.page || (resolved.page.normalizedPath !== this.page.normalizedPath)) {
            this.htmlTagHandler.unbindPage();
            this.htmlTagHandler.bindPage(resolved.page);
        }

        this.resolvedURL = resolved;
        this.page = resolved.page;

        if (!skipHistoryUpdate) {
            this.updateHistory(this.resolvedURL);
        }

        this.refreshCurrentPage().catch(console.error);
    }


    /**
     * 
     * @param {string} realUrl 
     * @returns {Promise<void>}
     */
    displayPageForUrlLater(realUrl) {
        return new Promise((resolve, _) => {
            this.displayPageForUrl(realUrl);
            resolve();
        });
    }

    /**
     * @public
     * @param {ClientConfig} config
     * @returns {Promise<ClientConfig>}
     */
    setConfig(config) {
        return new Promise((resolve, _) => {
            this.config = config;
            resolve(config);
        });
    }

    /**
     * @public
     */
    reset() {
        this.htmlTagHandler.reset();
        this.page = undefined;
        this.resolvedURL = undefined;
        this.config = undefined;
    }



    /**
     * Reloads the current page, possibly following a redirect (e.g. login).
     * Automatically handles change of page and avoids corrupting history.
     * 
     * @returns {Promise<void>}
     */
    refreshCurrentPage() {
        return this.client.loadPageData(this.resolvedURL).then(response => {
            const redirectedURL = this.urlResolver.resolve(response.nextPageURL);
            if (!redirectedURL) {
                throw new Error('No page found for URL: ' + response.nextPageURL);
            }

            const samePage = this.page ? redirectedURL.page.normalizedPath === this.page.normalizedPath : false;
            const sameResolvedUrl = redirectedURL.url === this.resolvedURL.url;

            // If redirect occurred, apply target page without history pollution
            if (!samePage || !sameResolvedUrl) {
                this.displayPageForUrl(redirectedURL.url, /* skipHistoryUpdate */ true);
                return;
            }

            const data = response.data;
            data.setValue(['pathVariables'], this.resolvedURL.pathVariablesAsMap());
            data.setValue(['urlParameters'], this.resolvedURL.urlParameters);
            this.page.data = data;
            this.htmlTagHandler.refresh(data);
        });
    }

    /**
     * @private
     * @returns {ResolvedURL}
     */
    welcomePageUrl() {
        // TODO Validate: Welcome Page must have no path-variables
        var welcomePage = this.pages.getWelcomePage();
        if (!welcomePage) return undefined;
        // normalizedPath works here, because welcome-page never has path-variables
        var path = new Path(new PathElement({ type: 'static', content: welcomePage.normalizedPath }));
        return new ResolvedURL(path, [], {}, welcomePage, path.normalized());
    }

    /**
     * Updates value displayed in browser's address-input.
     * @param {ResolvedURL} resolvedURL
     * @private
     */
    updateHistory(resolvedURL) {
        var title = this.htmlTagHandler.getTitle();
        app.history.appendPage(resolvedURL, title);
    }


}
