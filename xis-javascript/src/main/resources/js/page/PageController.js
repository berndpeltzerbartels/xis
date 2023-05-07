
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
        this.head = getElementByTagName('head');
        this.body = getElementByTagName('body');
        this.title = getElementByTagName('title');
        this.pageDataMap = {};
        this.pageAttributes = {};
        this.pageId = undefined;
        this.titleExpression;
    }

    /**
     * @public
     * @param {Config} config
     */
    displayInitialPage(config) {
        console.log('PageController - displayInitialPage');
        this.pageAttributes = config.pageAttributes;
        var _this = this;
        this.findPageForCurrentUrl()
            .then(page => _this.bindPage(page))
            .then(pageId => _this.refreshData(pageId))
            .then(pageId => _this.refreshPage(pageId))
            .catch(e => console.error(e));
    }

    bindPageById(id) {
        return this.bindPage(this.pages.getPageById(id));
    }

    /**
    * @returns {Promise<string>}
    */
    bindPage(page) {
        var _this = this;
        return new Promise((resolve, _) => {
            _this.clearHeadChildNodes();
            _this.clearBodyChildNodes();
            _this.clearBodyAttributes();
            _this.bindTitle(page);
            _this.bindHeadChildNodes(page.headChildArray);
            _this.bindBodyAttributes(page.bodyAttributes);
            _this.bindBodyChildNodes(page.bodyChildArray)
            resolve(page.id);
        }).catch(e => console.error('failed to bind page:' + e));
    }

    bindHeadChildNodes(nodeArray) {
        for (var node of nodeArray) {
            if (node.nodeType == 1 && node.localName == 'title') {
                continue;
            }
            this.head.appendChild(node);

        }
    }

    /**
     * 
     * @param {string} title 
     */
    bindTitle(page) {
        if (page.headChildArray) {
            var titleTag = page.headChildArray.find(child => child.localName == 'title');
            if (titleTag) {
                var title = titleTag.innerText;
                if (title.indexOf('${') != -1) {
                    this.titleExpression = new TextContentParser(title).parse();
                } else {
                    this.title.innerText = title;
                }
            }
        }
    }

    bindBodyChildNodes(nodeArray) {
        this.bindChildNodes(this.body, nodeArray);
    }

    bindChildNodes(element, nodeArray) {
        for (var node of nodeArray) {
            element.appendChild(node);
        }
    }

    bindBodyAttributes(attributes) {
        for (var name of Object.keys(attributes)) {
            this.body.setAttribute(name, attributes[name]);
        }
        this.initializer.initializeAttributes(this.body);
    }

    clearBodyAttributes() {
        for (var name of this.body.getAttributeNames()) {
            this.body.removeAttribute(name);
        }
        console.log('clearBodyAttributes:' + this.body);
        this.body._attributes = undefined;
    }

    clearHeadChildNodes() {
        this.clearChildren(this.head);
    }

    clearBodyChildNodes() {
        this.clearChildren(this.body);
    }

    clearChildren(element) {
        for (var node of nodeListToArray(element.childNodes)) {
            if (node.getAttribute && node.getAttribute('ignore')) {
                continue;
            }
            element.removeChild(node);
        }
    }

    unbindPage() {
        for (var name of this.body.getAttributeNames()) {
            this.body.removeAttribute(name);
        }
        for (var child of nodeListToArray(this.body.childNodes)) {
            if (!child.getAttribute || !child.getAttribute('ignore')) {
                this.body.removeChild(child);
            }
        }
        for (var child of nodeListToArray(this.head.childNodes)) {
            if (!child.getAttribute || !child.getAttribute('ignore')) {
                this.head.removeChild(child);
            }
        }
        this.titleExpression = undefined;
    }

    /**
     * @returns {Promise<string>}
     */
    findPageForCurrentUrl() {
        console.log('findPageId');
        return new Promise((resolve, _) => {
            var uri = document.location.pathname;
            var page = this.pages.getPageById(uri);
            if (!page) {
                page = this.pages.getWelcomePage();
            }
            if (page == null) {
                throw new Error('no page for uri:"' +uri+'" and no welcome page defined');
            }
            console.log('resolve findPageId: ' + uri);
            console.log('resolve findPageId: ' + page);
            resolve(page);
        });
    }

    /**
    * @private
    * @param {string} pageId
    * @returns {Promise<string>}
    */
    refreshData(pageId) {
        var _this = this;
        console.log('PageController - refreshData:' + pageId);
        var values = {};
        console.log('PageController - pageDataMap:' + this.pageDataMap);
        var pageData = this.pageDataMap[pageId];
        var pageAttributes = this.pageAttributes[pageId];
        if (pageData) {
            for (var dataKey of pageAttributes.modelsToSubmitForModel) {
                values[dataKey] = pageData.getValue([dataKey]);
            }
        }
        return this.client.loadPageData(pageId, values).then(response => {
            _this.pageDataMap[pageId] = new Data(response.data);
            return pageId;
        });
    }

    /**
    * @private
    * @param {string} pageId
    * @returns {Promise<string>}
    */
    refreshPage(pageId) {
        var _this = this;
        return new Promise((resolve, _) => {
            var data = _this.pageDataMap[pageId];
            _this.refreshTitle(data);
            refreshNode(_this.head, data);
            refreshNode(_this.body, data);
            //_this.updateHistory(pageId);
            console.log('resolve - refreshPage: ' + pageId);
            resolve(pageId);
        });
    }

    /**
     * @private
     * @param {Data} data
     */
    refreshTitle(data) {
        if (this.titleExpression) {
            this.title.innerText = this.titleExpression.evaluate(data);
        }
    }

    /**
     * @private
     * Changes value diplayed in browsers address-input.
     * @param {string} pageId
     */
    updateHistory(pageId) {
        console.log('update history');
        var title = getElementByTagName('title').innerText;
        window.history.replaceState({}, title, pageId);
    }
}
