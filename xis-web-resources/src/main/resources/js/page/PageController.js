
class PageController {

    /**
     * @param {Client} client
     * @param {Pages} pages
     */
    constructor(client, pages) {
        this.client = client;
        this.pages = pages;
        this.html = getElementByTagName('html');
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

    bindPageForId(id) {
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
            _this.bindHeadChildNodes(page.headChildArray);
            _this.bindBodyAttributes(page.bodyAttributes);
            _this.bindBodyChildNodes(page.bodyChildArray)
            resolve(page.id);
        });
    }

    bindHeadChildNodes(nodeArray) {
        this.bindChildNodes(this.head, nodeArray);
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
            this.setAttribute(name, attributes[name]);
        }
    }

    clearBodyAttributes() {
        for (var name of this.body.getAttributeNames()) {
            this.body.removeAttribute(name);
        }
    }


    clearHeadChildNodes() {
        this.clearChildren(this.head);
    }

    clearBodyChildNodes() {
        this.clearChildren(this.head);
    }

    clearChildren(element) {
        for (var node of nodeListToArray(element.childNodes)) {
            if (node.getAttribute && node.getAttribute('ignore')) {
                continue;
            }
            if (node.parentNode) {
                node.parentNode.removeChild(node);
            }
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
        this.titleExpression = '';
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
            console.log('resolve findPageId: ' + uri);
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
        console.log('refreshData');
        var values = {};
        var pageData = this.pageDataMap[pageId];
        var pageAttributes = this.pageAttributes[pageId];
        if (pageData) {
            for (var dataKey of pageAttributes.modelsToSubmitForModel) {
                values[dataKey] = pageData.getValue([dataKey]);
            }
        }
        return client.loadPageData(pageId, values).then(response => {
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
            _this.head._refresh(data);
            _this.body._refresh(data);
            _this.updateHistory(pageId);
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
            this.title.innerHTML = this.titleExpression.evaluate(data);
        }
    }

    /**
     * @private
     * Changes value diplayed in browsers address-input.
     * @param {string} pageId
     */
    updateHistory(pageId) {
        var title = getElementByTagName('title').innerHTML;
        window.history.replaceState({}, title, pageId);
    }
}
