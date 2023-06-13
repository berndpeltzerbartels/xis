
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
     * @private
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
     * @public
     * @param {Config} config
     */
    displayInitialPage(config) {
        console.log('PageController - displayInitialPage');
        this.config = config;
        return this.displayPage(document.location.pathname);
    }


    /**
     * @public
     * @param {string} pageId
     */
    displayPage(pageId) {
        var _this = this;
        this.findPageForUrl(pageId)
            .then(page => _this.doBindPage(page))
            .then(() => _this.refreshData())
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
     * @public
     * @param {Array<Parameter>} parameters, may be undefined
     */
    reloadDataAndRefreshCurrentPage(parameters) {
        if (!this.pageId) throw new Error('no page to reload');
        var _this = this;
        this.refreshData(parameters).then(() => _this.refreshPage())
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
     * @private
     * @param {string} pageId
     */
    updateHistory(pageId) {
        console.log('update history');
        var title = getElementByTagName('title').innerText;
        window.history.replaceState({}, title, pageId);
    }

}
