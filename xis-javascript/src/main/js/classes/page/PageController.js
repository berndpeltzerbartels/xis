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
        this.currentResponse = undefined;
    }

    /**
     * @public
     * @returns {Promise<void>}
     */
    initBuffer() {
        return this.htmlTagHandler.bodyTagHandler.initBuffer();
    }

    /**
     * @public
     * @returns {Promise<void>}
     */
    commitBuffer() {
        this.htmlTagHandler.bodyTagHandler.commitBuffer();
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
            .then(response => this.handleActionResponse(response))
            .catch(error => handleError(error));
    }

    /**
     * Handels server-response after submitting an action.
     * 
     * @public
     * @param {ServerResponse} response
     * @returns {Promise<void>}
     */
    handleActionResponse(response) {
        this.handleActionResponseNoContent(response);
        if (response.status == 204) {
            return Promise.resolve(rv);
        }
        var data = response.data;
        const pathname = this.resolvedURL.url.split('?')[0]; // Remove query string
        data.setValue(['pathVariables'], this.resolvedURL.pathVariablesAsMap());
        data.setValue(['urlParameters'], this.resolvedURL.urlParameters);
        data.setValue(['url'], this.resolvedURL.url);
        data.setValue(['pathname'], pathname);
        data.setValue(['queryParams'], this.resolvedURL.urlParameters);
        this.page.data = data;

        return this.initBuffer()
            .then(() => this.htmlTagHandler.refresh(this.page.data))
            .then(() => {if (isSet(response.annotatedTitle)) this.setTitle(response.annotatedTitle);})
            .then(() => {if (isSet(response.annotatedAddress)) this.setAddress(response.annotatedAddress);})
            .then(() => this.commitBuffer());
    }

    /** 
     * Handels server-response after submitting an action.
     * 
     * @public
     * @param {ServerResponse} response
     * @returns {boolean} true, if page was replaced
     */
    handleActionResponseNoContent(response) {
        var rv = false;
        if (response.nextURL) {
            var resolvedURL = this.urlResolver.resolve(response.nextURL);
            if (!resolvedURL) {
                throw new Error('no page for ' + response.nextURL);
            }
            this.resolvedURL = resolvedURL;
            if (resolvedURL.page != this.page) {
                this.page = resolvedURL.page;
                this.htmlTagHandler.unbindPage();
                this.htmlTagHandler.bindPage(resolvedURL.page);
                rv = true;
            }
        }
        if (response.status < 300) {
            this.updateHistory(this.resolvedURL, response.annotatedTitle);
        }
        return rv;
    }

     /**
     * @public
     * @param {Array<String>} eventIds
     * @returns {Promise<boolean>}
     */
    handleUpdateEvents(eventIds) {
        if (!this.resolvedURL) {
            return Promise.resolve(false);
        }
        for (const eventId of eventIds) {
            if (this.resolvedURL.page.updateEventKeys.includes(eventId)) {
                return this.handleUpdateEvent().then(() => true);
            }
        }
        return Promise.resolve(false);
    }

    /**
     * @private
     * @returns {Promise<void>}
     */
    handleUpdateEvent() {
        return this.displayPageForResolvedURL(this.resolvedURL, /* skipHistoryUpdate */ true);
    }

    getData() {
        return this.page ? this.page.data : undefined;
    }

    /**
     * Displays a page from a given URL.
     * Optionally skips browser history update (used e.g. for popstate navigation).
     * 
     * @param {string} realUrl
     * @param {boolean} [skipHistoryUpdate=false]
     * @returns {Promise<void>}
     */
    /**
   * Displays a page from a given URL.
   * Optionally skips browser history update (used e.g. for popstate navigation).
   *
   * @param {string} realUrl
   * @param {boolean} [skipHistoryUpdate=false]
   * @returns {Promise<void>}
   */
    displayPageForUrl(realUrl, skipHistoryUpdate = false) {
        const resolved = this.urlResolver.resolve(realUrl) || this.welcomePageUrl();
        if (!resolved) {
            throw new Error("Cannot resolve URL: " + realUrl);
        }
        return this.displayPageForResolvedURL(resolved, skipHistoryUpdate)
            .catch(error => handleError(error));
    }

    /**
     *
     * @param {ResolvedURL} resolved
     * @param {boolean} skipHistoryUpdate
     * @returns {Promise<void>}
     */
    displayPageForResolvedURL(resolved, skipHistoryUpdate = false) {
        return this.client.loadPageData(resolved).then(response => {
            this.currentResponse = response;
            if (response.nextURL) {
                const nextResolved = this.urlResolver.resolve(response.nextURL);
                if (resolved.normalizedPath !== nextResolved.normalizedPath) {
                    // Redirect – do not pollute browser history
                    return this.displayPageForUrl(response.nextURL, true);
                }
            }
            this.resolvedURL = resolved;
            this.page = resolved.page;

            const data = response.data;
            const pathname = resolved.url.split('?')[0]; // Remove query string
            data.setValue(['pathVariables'], this.resolvedURL.pathVariablesAsMap());
            data.setValue(['urlParameters'], this.resolvedURL.urlParameters);
            data.setValue(['url'], this.resolvedURL.url);
            data.setValue(['pathname'], pathname);
            data.setValue(['queryParams'], this.resolvedURL.urlParameters);
            this.page.data = data;

            this.htmlTagHandler.unbindPage();
            this.htmlTagHandler.bindPage(this.page);

            this.initBuffer()
                .then(() => this.htmlTagHandler.refresh(data))
                .then(() => {if (response.annotatedTitle) this.setTitle(response.annotatedTitle);})
                .then(() => this.commitBuffer())
                .then(() =>  app.eventPublisher.publish(EventType.BUFFER_COMMITTED))
                .then(() => {
                    if (!skipHistoryUpdate && response.status < 300) {
                        this.updateHistory(this.resolvedURL, response.annotatedTitle);
                    }
                    app.eventPublisher.publish(EventType.PAGE_LOADED, { page: this.page, url: this.resolvedURL });
                });
        }).catch(error => handleError(error));
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
            const nextUrl = response.nextURL;
            if (nextUrl) {
                const nextResolved = this.urlResolver.resolve(nextUrl);
                if (!nextResolved) {
                    throw new Error("Cannot resolve redirected URL: " + nextUrl);
                }
                if (nextResolved.normalizedPath !== this.resolvedURL.normalizedPath) {
                    return this.displayPageForResolvedURL(nextResolved, /* skipHistoryUpdate */ true);
                }
            }
            const data = response.data;
            data.setValue(['pathVariables'], this.resolvedURL.pathVariablesAsMap());
            data.setValue(['urlParameters'], this.resolvedURL.urlParameters);
            this.page.data = data;
            this.htmlTagHandler.refresh(data);
        }).catch(error => handleError(error));
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

    setTitle(title) {
        this.htmlTagHandler.headTagHandler.setTitle(title);
    }

    /**
     * Updates value displayed in browser's address-input.
     * @param {ResolvedURL} resolvedURL
     * @param {string} annotatedAddress - Optional custom address from @Address annotation
     * @param {string} annotatedTitle - Optional custom title from @Title annotation
     * @private
     */
    updateHistory(resolvedURL, annotatedTitle) {
        var title = annotatedTitle || this.htmlTagHandler.getTitle();
        app.history.appendPage(resolvedURL.url, title);
    }


    setAddress(url) {
      app.history.appendPage(url, this.htmlTagHandler.getTitle());
    }


}
