
class Controller {

    /**
     * 
     * @param {Client} client 
     */
    constructor(client) {
        this.client = client;
        this.html = this.getElementByTagName('html');
        this.head = this.getElementByTagName('head');
        this.body = this.getElementByTagName('body');
        this.title = this.getElementByTagName('title');
        this.widgets = {};
        this.pages = {};
        this.pageData = {};
        this.headChildNodes = [];
        this.welcomePageId;
    }

    init() {
        var _this = this;
        this.loadConfig().then(config => {
            _this.welcomePageId = config.welcomePageId;
            var promises = [];
            config.pageIds.forEach(id => { _this.pages[id] = {}; _this.pageData[id] = {}; });
            config.widgetIds.forEach(id => _this.widgets[id] = {});
            config.widgetIds.forEach(id => promises.push(_this.loadWidget(id)));
            config.pageIds.forEach(id => promises.push(_this.loadPageHead(id)));
            config.pageIds.forEach(id => promises.push(_this.loadPageBody(id)));
            config.pageIds.forEach(id => promises.push(_this.loadPageBodyAttributes(id)));
            return Promise.all(promises);

        }).then(() => _this.findPageId())
            .then(pageId => _this.bindPage(pageId))
            .then(pageId => _this.refreshData(pageId))
            .then(pageId => _this.refreshPage(pageId));
    }


    /**
    * @returns {Promise<string>}
    */
    bindPage(pageId) {
        var _this = this;
        return new Promise((resolve, _) => {
            var headChildArray = _this.pages[pageId].headChildArray;
            var bodyChildArray = _this.pages[pageId].bodyChildArray;
            var attributes = _this.pages[pageId].bodyAttributes;
            this.bindHead(headChildArray);
            this.bindBody(bodyChildArray, attributes);
            resolve(pageId);
        });
    }

    unbindWidget(container) {
        container.innerHTML = undefined;
    }

    unbindPage() {
        // TODO Do not remove head or title or body
        // TODO Do not remove elements with attribute "data-ignore"
        for (var name of this.body.getAttributeNames()) {
            body.removeAttribute(name);
        }
    }

    pageAction(pageId, action) {
        var _this = this;
        return this.client.pageAction(pageId, action, this.pageData[pageId]).then(response => {
            var data = response.data;
            for (var key of Object.keys(data)) {
                _this.pageData[pageId][key] = data[key];
            }
            return response.nextControllerId;
        }).then(controllerId => _this.bindPage(controllerId));
    }


    /**
     * @returns {Promise<string>}
     */
    findPageId() {
        var _this = this;
        return new Promise((resolve, _) => {
            var uri = document.location.pathname;
            if (!_this.pages[uri]) {
                uri = _this.welcomePageId;
            }
            resolve(uri);
        });
    }


    /**
     * @returns {Promise<string>}
     */
    refreshData(pageId) {
        var controller = this;
        return client.loadPageData(pageId, controller.pageData[pageId] || {}).then(response => {
            var data = response.data;
            for (var key of Object.keys(data)) {
                controller.pageData[pageId][key] = data[key];
            }
            return pageId;
        });
    }

    /**
    * @returns {Promise<string>}
    */
    refreshPage(pageId) {
        var _this = this;
        return new Promise((resolve, _) => {
            var data = _this.pageData[pageId];
            this.refreshChildNodes(_this.head, data);
            this.refreshChildNodes(_this.body, data);
            _this.updateHistory(_this.head, pageId);
            resolve(pageId);
        });
    }


    refreshChildNodes(element, data) {
        for (var i = 0; i < element.childNodes.length; i++) {
            var child = element.childNodes.item(i);
            if (!child.getAttribute || !child.getAttribute('data-ignore')) {
                refresh(child, data);
            }
        }
    }

    updateHistory(head, pageId) {
        var titleList = head.getElementsByTagName('title');
        var title = titleList.length > 0 ? titleList.item(0).innerHTML : '';
        window.history.pushState({}, title, pageId);
    }


    /**
     * @param {string} pageId
    * @returns {Promise<string>}
    */
    loadPageHead(pageId) {
        var _this = this;
        return this.client.loadPageHead(pageId).then(content => {
            var holder = document.createElement('div');
            holder.innerHTML = content;
            initialize(holder);
            _this.pages[pageId].headChildArray = toArray(holder.childNodes);
            return pageId;
        });
    }

    /**
    * @param {string} pageId
    * @returns {Promise<string>}
    */
    loadPageBody(pageId) {
        var _this = this;
        return this.client.loadPageBody(pageId).then(content => {
            var holder = document.createElement('div');
            holder.innerHTML = content;
            initialize(holder);
            _this.pages[pageId].bodyChildArray = toArray(holder.childNodes);
            return pageId;
        });
    }

    loadPageBodyAttributes(pageId) {
        var _this = this;
        return this.client.loadPageBodyAttributes(pageId).then(attributes => {
            _this.pages[pageId].bodyAttributes = attributes;
            return pageId;
        });
    }
    /**
    * @returns {Promise<string>}
    */
    loadWidget(widgetId) {
        var _this = this;
        return this.client.loadWidget(widgetId).then(widgetHtml => {
            var root = _this.asRootElement(widgetHtml);
            _this.widgets[widgetId].root = root;
            initialize(root);
            return widgetId;
        });
    }


    /**
    * @returns {Promise<ComponentConfig>}
    */
    loadConfig() {
        return this.client.loadConfig();
    }

    /**
     *
     * @param {string} tree
     * @returns {Element}
     */
    asRootElement(tree) {
        var div = document.createElement('div');
        div.innerHTML = trim(tree);
        return div.childNodes.item(0);
    }
    getElementByTagName(name) {
        return document.getElementsByTagName(name).item(0);
    }

    /**
    * @param {array<Node>} headChildArray 
    */
    bindHead(headChildArray) {
        for (var i = 0; i < headChildArray.length; i++) {
            var child = headChildArray[i];
            if (isElement(child) && child.localName == 'title') {
                getTemplateTitle().innerHTML = child.innerHTML;
            } else {
                this.head.appendChild(child);
            }

        }
    }

    /**
     * @param {array<Node>} bodyChildArray
     * @param {attributes} any
     */
    bindBody(bodyChildArray, attributes) {
        for (var name of Object.keys(attributes)) {
            this.body.setAttribute(name, attributes[name]);
        }
        for (var i = 0; i < bodyChildArray.length; i++) {
            this.body.appendChild(bodyChildArray[i]);
        }
    }

}


