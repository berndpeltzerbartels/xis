/**
 * 
 * @typedef PageController
 * @property {Client} client
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
     * @param {Client} client
     * @param {Pages} pages
     * @param {Initializer} initializer
     * @param {URLResolver} urlResolver
     */
    constructor(client, pages, initializer, urlResolver) {
        this.client = client;
        this.pages = pages;
        this.initializer = initializer;
        this.urlResolver = urlResolver;
        this.htmlTagHandler = new HtmlTagHandler();
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
     * @returns {Promise<void>}
     */
    submitAction(action) {
        var _this = this;
        return this.client.pageAction(this.resolvedURL, this.page.data, action)
            .then(response => _this.handleActionResponse(response));
    }

    /**
   * Should be used in case of the source 
   * of the action has no parent widget-container
   * and invoker has no data.
   * 
   * @see FormHandler
   * 
   * @public
   * @param {String} action
   * @param {Data} formData
   * @returns {Promise<void>}
   */
    submitFormAction(action, formData) {
        var _this = this;
        return this.client.pageFormAction(this.resolvedURL, this.page.data, action, formData)
            .then(response => _this.handleActionResponse(response));
    }

    /**
     * Handels server-response after submitting an action.
     * 
     * @public
     * @param {Response} response
     */
    handleActionResponse(response) {
        if (response.nextPageURL) {
            var resolvedURL = this.urlResolver.resolve(response.nextPageURL);
            if (!resolvedURL) {
                throw new Error('no page for ' + response.nextPageURL);
            }
            this.resolvedURL = resolvedURL;
            if (resolvedURL.page != this.page) {
                this.page = resolvedURL.page;
                this.htmlTagHandler.bindPage(resolvedURL.page);
            }
        }
        var data = response.data;
        data.setValue(['pathVariables'], resolvedURL.pathVariablesAsMap());
        data.setValue(['urlParameters'], resolvedURL.urlParameters);
        this.page.data = data;
        this.htmlTagHandler.refresh(this.page.data);
        this.updateHistory(this.resolvedURL);
    }


    getData() {
        return this.page ? this.page.data : undefined;
    }

    /**
     * Displays page by it's location from
     * browser's address-field.
     * 
     * @public
     * @param {string} realUrl url from address-line
     */
    displayPageForUrl(realUrl) {
        this.resolvedURL = this.urlResolver.resolve(realUrl);
        if (!this.resolvedURL) {
            this.resolvedURL = this.welcomePageUrl();
        }
        if (this.resolvedURL.page != this.page) {
            this.htmlTagHandler.bindPage(this.resolvedURL.page);
        }
        this.page = this.resolvedURL.page;
        this.refreshCurrentPage().catch(e => console.error(e));
    }

    /**
     * 
     * @param {string} realUrl 
     * @returns {Promise<void>}
     */
    displayPageForUrlLater(realUrl) {
        var _this = this;
        return new Promise((resolve, _) => {
            _this.displayPageForUrl(realUrl);
            resolve();
        });
    }

    /**
     * @public
     * @param {ClientConfig} config
     * @returns {Promise<ClientConfig>}
     */
    setConfig(config) {
        var _this = this;
        return new Promise((resolve, _) => {
            _this.config = config;
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
    * @private
    * @param {ResolvedURL} resolvedURL
    * @returns {Promise<string>}
    */
    refreshCurrentPage() {
        var _this = this;
        return this.client.loadPageData(this.resolvedURL, this.page.data).then(response => {
            var data = response.data;
            data.setValue(['pathVariables'], this.resolvedURL.pathVariablesAsMap());
            data.setValue(['urlParameters'], this.resolvedURL.urlParameters);
            this.page.data = data;
            _this.htmlTagHandler.refresh(data);
        });
    }

    /**
     * @private
     * @returns {ResolvedURL}
     */
    welcomePageUrl() {
        // TODO Validate: Welcome Page must have no path-variables
        var welcomePage = this.pages.getWelcomePage();
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
        window.history.replaceState({}, title, resolvedURL.url);
    }


}
