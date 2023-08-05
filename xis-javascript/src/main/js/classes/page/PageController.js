/**
 * 
 * @typedef PageController
 * @property {Client} client
 * @property {Pages} pages
 * @property {Initializer} initializer
 * @property {URLResolver} urlResolver
 * @property {Page} page
 * @property {PageHtml} html
 * @property {Data} data
 * @property {Config} config
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
     * @param {PageHtml} html
     */
    constructor(client, pages, initializer, urlResolver, html) {
        this.client = client;
        this.pages = pages;
        this.initializer = initializer;
        this.urlResolver = urlResolver;
        this.html = html;
        this.page = undefined;
        this.resolvedURL = undefined;
        this.data = new Data({});
        this.config = undefined;
    }

    /**
     * Should be used for any action 
     * having a page-controller for target (instead of a widget).
     * This occurs in case of the source 
     * of the action has no parent widget-container.
     * 
     * @public
     * @param {String} action
     * @param {array<Parameter>} data
     * @returns {Promise<void>}
     */
    submitAction(action, parameters) {
        var _this = this;
        var clientData = new PageClientData();
        clientData.pathVariables = this.resolvedURL.pathVariables;
        clientData.urlParameters = this.resolvedURL.urlParameters
        clientData.parameters = parameters;
        clientData.modelData = this.modelDataForAction(action);
        return this.client.pageAction(this.page.normalizedPath, clientData, action)
            .then(response => _this.handleActionResponse(response));
    }

    /**
     * Handels server-response after subitting an action.
     * 
     * @public
     * @param {Response} response
     */
    handleActionResponse(response) {
        var data = new Data(response.data);
        if (response.nextPageURL) {
            debugger;
            var resolvedURL = this.urlResolver.resolve(response.nextPageURL);
            if (!resolvedURL) {
                throw new Error('no page for ' + response.nextPageURL);
            }
            this.resolvedURL = resolvedURL;
            if (resolvedURL.page != this.page) {
                this.html.bindPage(resolvedURL.page);
            }

        }
        data.setValue('pathVariables', this.resolvedURL.pathVariables);
        data.setValue('urlParameters', this.resolvedURL.urlParameters);
        this.html.refresh(data, this.resolvedURL);
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
            this.html.bindPage(this.resolvedURL.page);
        }
        this.page = this.resolvedURL.page;
        this.refreshCurrentPage().catch(e => console.error(e));
    }

    /**
     * @public
     * @param {Config} config 
     */
    setConfig(config) {
        this.config = config;
    }

    /**
     * @public
     */
    reset() {
        this.page = undefined;
        this.resolvedURL = undefined;
        this.data = new Data({});
        this.config = undefined;
    }

    /**
    * @private
    * @param {ResolvedURL} resolvedURL
    * @returns {Promise<string>}
    */
    refreshCurrentPage() {
        var _this = this;
        var pathVariables = this.resolvedURL.pathVariables;
        var urlParameters = this.resolvedURL.urlParameters;
        var clientData = new PageClientData();
        clientData.pathVariables = this.resolvedURL.pathVariables;
        clientData.urlParameters = this.resolvedURL.urlParameters
        clientData.parameters = [];
        clientData.modelData = this.modelDataForRefresh();
        return this.client.loadPageData(this.page.normalizedPath, clientData).then(response => {
            var data = _this.responseAsData(response, pathVariables, urlParameters);
            _this.data = data;
            _this.html.refresh(data, this.resolvedURL);
        });
    }

    /**
     * @private
     * @returns {string: string}
     */
    modelDataForRefresh() {
        var result = {};
        var attributes = this.config.pageAttributes[this.page.normalizedPath];
        var keys = attributes.modelsToSubmitOnRefresh;
        for (var key of keys) {
            result[key] = this.data.getValue([key]);
        }
        return result;
    }

    /**
    * @private
    * @param {string} action
    * @returns {string: string}
    */
    modelDataForAction(action) {
        debugger;
        var result = {};
        var attributes = this.config.pageAttributes[this.page.normalizedPath];
        var keys = attributes.modelsToSubmitOnAction[action];
        for (var key of keys) {
            result[key] = this.data.getValue([key]);
        }
        return result;
    }

    /**
     * @private 
     * @param {Response} response 
     * @param {Arrays<string: string} pathVariableArray
     * @param {string:string} urlParameters 
     * @returns {Data}
     */
    responseAsData(response, pathVariableArray, urlParameters) {
        var pathVariables = {};
        for (var keyValue of pathVariableArray) {
            var key = Object.keys(keyValue)[0]
            var value = Object.values(keyValue)[0];
            pathVariables[key] = value;
        }
        var data = new Data(response.data);
        data.setValue('pathVariables', pathVariables);
        data.setValue('parameters', response.parameters);
        data.setValue('urlParameters', urlParameters);
        return data;
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
        return new ResolvedURL(path, [], [], welcomePage);
    }

}
