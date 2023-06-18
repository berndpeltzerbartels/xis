/**
 * A singleton responsibe for placing or replacing
 * subelements of the root-page that will never 
 * get replaced.
 */
class PageController {

    /**
     * @param {Client} client
     * @param {Pages} pages
     * @param {Initializer} initializer
     */
    constructor(client, pages, initializer) {
        this.client = client;
        this.pages = pages;
        this.initializer = initializer;
        this.pageDataMap = {};
        this.config = {};
        this.pageId = undefined;
        this.titleExpression;
    }

    /**
     * Should be used, for any action 
     * having a page-controller for target.
     * This is in case of the source 
     * of the action has no parent widget-container.
     * 
     * @public
     * @param {String} action 
     * @returns {Promise<void>}
     */
    submitAction(action) {
        var _this = this;
        var values = {};
        console.log('PageController - pageDataMap:' + this.pageDataMap);
        var pageData = this.pageDataMap[this.pageId];
        var pageAttributes = this.config.pageAttributes[this.pageId];
        if (pageData) {
            for (var dataKey of pageAttributes.modelsToSubmitOnAction[action]) {
                values[dataKey] = pageData.getValue([dataKey]);
            }
        }
        return this.client.pageAction(this.pageId, action, values)
            .then(response => _this.handleActionResponse(response));
    }



    /**
     * Handels server-response after subitting an action.
     * 
     * @public
     * @param {Response} response 
     */
    handleActionResponse(response) {
        if (response.nextWidgetId) throw new Error('widget can not be set if there is no container: ' + response.nextWidgetId);
        this.pageDataMap[response.nextPageId || this.pageId] = new Data(response.data);
        if (response.nextPageId && response.nextPageId !== this.pageId) {
            var _this = this;
            this.pageId = response.nextPageId;
            this.findPageForUrl(this.pageId)
                .then(page => _this.doBindPage(page))
                .then(() => _this.refreshPage())
                .catch(e => console.error(e));

        } else {
            this.refreshPage();
        }
    }


    /**
     * Reads the welcome-page from config and initiates
     * diplaying it.
     * 
     * @public
     * @param {Config} config
     */
    displayInitialPage(config) {
        console.log('PageController - displayInitialPage');
        this.config = config;
        return this.displayPage(config.welcomePageId);
    }


    /**
     * @public
     * @param {string} pageId
     * @param {any} parameters
     * @returns {Promise<void>}
     */
    displayPage(pageId, parameters) {
        var _this = this;
        if (this.pageId == pageId) {
            this.pageId = pageId;
            return this.refreshData(parameters)
                .then(() => _this.refreshPage())
                .catch(e => console.error(e));
        }
        var _this = this;
        return this.findPageForUrl(pageId)
            .then(page => _this.doBindPage(page))
            .then(() => _this.refreshData(parameters))
            .then(() => _this.refreshPage())
            .catch(e => console.error(e));
    }

    /**
    * @public
    * @returns {Promise<void>}
    */
    bindPage(id) {
        this.pageId = id;
        var page = this.pages.getPageById(id);
        if (!page) throw new Error('no such page: ' + id);
        return this.doBindPage(page);
    }

    /**
    * @private
    * @returns {Promise<void>}
    */
    doBindPage(page) {
        var _this = this;
        this.pageId = page.id;
        return new Promise((resolve, _) => {
            var head = getElementByTagName('head');
            var body = getElementByTagName('body');
            _this.clearChildren(head);
            _this.clearChildren(body);
            _this.clearBodyAttributes(body);
            _this.bindTitle(page);
            _this.bindHeadChildNodes(head, page.headChildArray);
            _this.bindBodyAttributes(body, page.bodyAttributes);
            _this.bindBodyChildNodes(body, page.bodyChildArray);
            resolve();
        });
    }


    /**
     * @private
     * @param {Element} element 
     * @param {Array<Node>} attributes 
     */
    bindHeadChildNodes(head, nodeArray) {
        for (var node of nodeArray) {
            if (node.nodeType == 1 && node.localName == 'title') {
                continue;
            }
            head.appendChild(node);

        }
    }

    /**
     * Loads the title of the page from server into 
     * the root-page and converts it to an expresion.
     * 
     * @private
     * @param {Page} title 
     */
    bindTitle(page) {
        var title = page.title;
        if (title) {
            if (title.indexOf('${') != -1) {
                this.titleExpression = new TextContentParser(title).parse();
                return;
            }
        }
        this.setTitle(title);
    }

    /**
     * Setting the title after refreshing the page and 
     * evaluating the expression, so it's the "real text"
     * diplayed for title.
     * 
     * @private
     * @param {string} title 
     */
    setTitle(title) {
        var titleElement = getElementByTagName('title');
        if (titleElement.setInnerText) {
            titleElement.setInnerText(title);
        } else {
            titleElement.innerText = title;
        }
    }

    /**
     * @private
     * @param {Element} element 
     * @param {Array<Node>} attributes 
     */
    bindBodyChildNodes(body, nodeArray) {
        this.bindChildNodes(body, nodeArray);
    }

    /**
     * @private
     * @param {Element} element 
     * @param {Array<Node>} attributes 
     */
    bindChildNodes(element, nodeArray) {
        for (var node of nodeArray) {
            element.appendChild(node);
        }
    }

    /**
     * @private
     * @param {Element} body 
     * @param {any} attributes 
     */
    bindBodyAttributes(body, attributes) {
        for (var name of Object.keys(attributes)) {
            body.setAttribute(name, attributes[name]);
        }
        app.initializer.initializeAttributes(body);
    }

    /**
     * Removes all attributes from body-tag except onload.
     *
     * @private
     * @param {Element} body 
     */
    clearBodyAttributes(body) {
        for (var name of body.getAttributeNames()) {
            if (name == 'unload') {
                continue;
            }
            body.removeAttribute(name);
        }
        console.log('clearBodyAttributes:' + body);
        body._attributes = undefined;
    }

    /**
     * Removes all child nodes.
     * 
     * @private
     * @param {Element} element 
     */
    clearChildren(element) {
        for (var node of nodeListToArray(element.childNodes)) {
            if (node.getAttribute && node.getAttribute('ignore')) {
                continue;
            }
            element.removeChild(node);
        }
    }

    /**
     * @private
     * @returns {Promise<string>}
     */
    findPageForUrl(uri) {
        console.log('findPageId');
        return new Promise((resolve, _) => {
            var page = this.pages.getPageById(uri);
            if (!page) {
                page = this.pages.getWelcomePage();
            }
            if (page == null) {
                throw new Error('no page for uri:"' + uri + '" and no welcome page defined');
            }
            resolve(page);
        });
    }

    /**
    * @private
    * @param {Array<Parameter>} parameters, may be undefined
    * @returns {Promise}
    */
    refreshData(parameters = []) {
        var _this = this;
        var pageId = this.pageId;
        console.log('PageController - refreshData:' + pageId);
        var values = {};
        console.log('PageController - pageDataMap:' + this.pageDataMap);
        var pageData = this.pageDataMap[pageId];
        var pageAttributes = this.config.pageAttributes[pageId];
        if (pageData) {
            for (var dataKey of pageAttributes.modelsToSubmitOnRefresh) {
                values[dataKey] = pageData.getValue([dataKey]);
            }
        }
        var params = {};
        if (parameters) {
            for (var par of parameters) {
                params[pageAttributes.name] = par.value;
            }
        }
        return this.client.loadPageData(pageId, values, params).then(response => {
            _this.pageDataMap[pageId] = new Data(response.data);
            return pageId;
        });
    }


    /**
     * @public
     * @returns {Data}
     */
    getCurrentPageData() {
        return this.pageDataMap[this.pageId];
    }

    /**
     *
     * Resetting of controller and root-page intended to use
     * within tests.
     * 
     *  @public
     */
    reset() {
        var body = getElementByTagName('body');
        var head = getElementByTagName('head');

        for (var name of body.getAttributeNames()) {
            body.removeAttribute(name);
        }
        for (var child of nodeListToArray(body.childNodes)) {
            if (!child.getAttribute || !child.getAttribute('ignore')) {
                body.removeChild(child);
            }
        }
        for (var child of nodeListToArray(head.childNodes)) {
            if (!child.getAttribute || !child.getAttribute('ignore')) {
                head.removeChild(child);
            }
        }
        var titleTag = getElementByTagName('title');

        this.titleExpression = undefined;
        titleTag.innerText = ''
        this.pageDataMap = {};
        this.config = {};
        this.pageId = undefined;
    }

    /**
    * @private
    * @returns {Promise<string>}
    */
    refreshPage() {
        var _this = this;
        return new Promise((resolve, _) => {
            var data = _this.pageDataMap[this.pageId];
            _this.refreshTitle(data);
            refreshNode(getElementByTagName('head'), data);
            refreshNode(getElementByTagName('body'), data);
            _this.updateHistory(this.pageId);
            console.log('resolve - refreshPage: ' + this.pageId);
            resolve();
        });
    }

    /**
     * @private
     * @param {Data} data
     */
    refreshTitle(data) {
        if (this.titleExpression) {
            var content = this.titleExpression.evaluate(data);
            this.setTitle(content);
        }
    }

    /**
     * Changes value diplayed in browsers address-input.
     * 
     * @private
     * @param {string} pageId
     */
    updateHistory(pageId) {
        console.log('update history');
        var title = getElementByTagName('title').innerText;
        window.history.replaceState({}, title, pageId);
    }

}
