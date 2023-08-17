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
     * @returns {Promise<void>}
     */
    submitAction(action) {
        var _this = this;
        var clientData = new ClientData();
        clientData.pathVariables = this.pathVariablesAsMap();
        clientData.urlParameters = this.resolvedURL.urlParameters
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
        var data = response.data;
        if (response.nextPageURL) {
            var resolvedURL = this.urlResolver.resolve(response.nextPageURL);
            if (!resolvedURL) {
                throw new Error('no page for ' + response.nextPageURL);
            }
            this.resolvedURL = resolvedURL;
            if (resolvedURL.page != this.page) {
                this.html.bindPage(resolvedURL.page);
            }

        }
        data.setValue('pathVariables', this.pathVariablesAsMap());
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
        this.page = undefined;
        this.resolvedURL = undefined;
        this.data = new Data({});
        this.config = undefined;
    }

    /**
     * Merges the array of path-variables into a map.
     * 
     * @public
     * @returns {{string: string}}
     */
    pathVariablesAsMap() {
        var map = {};
        for (var pathVariable of this.resolvedURL.pathVariables) {
            var name = Object.keys(pathVariable)[0];
            var value = Object.values(pathVariable)[0];
            map[name] = value;
        }
        return map;
    }

    /**
     * @private
     * @returns {ClientData}
     */
    clientData() {
        var clientData = new ClientData();
        clientData.urlParameters = this.resolvedURL.urlParameters
        clientData.pathVariables = this.pathVariablesAsMap();
        clientData.modelData = this.modelDataForRefresh();
        return clientData;
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
        var clientData = this.clientData();
        return this.client.loadPageData(this.page.normalizedPath, clientData).then(response => {
            var data = _this.responseToData(response, pathVariables, urlParameters);
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
        var result = {};
        var attributes = this.config.pageAttributes[this.page.normalizedPath];
        var keys = attributes.modelsToSubmitOnAction[action];
        if (keys) {
            for (var key of keys) {
                result[key] = this.data.getValue([key]);
            }
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
    responseToData(response, pathVariableArray, urlParameters) {
        var pathVariables = {};
        for (var keyValue of pathVariableArray) {
            var key = Object.keys(keyValue)[0]
            var value = Object.values(keyValue)[0];
            pathVariables[key] = value;
        }
        var data = response.data;
        data.setValue('pathVariables', pathVariables);
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
        return new ResolvedURL(path, [], {}, welcomePage);
    }

}
