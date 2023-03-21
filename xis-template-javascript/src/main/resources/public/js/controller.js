

class PageController {

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
        this.head._xis = new XisElement(this.head);
        this.body._xis = new XisElement(this.body);
        this.pages = {};
        this.pageData = {};
        this.timestamps = {};
        this.pageId = undefined;
        this.welcomePageId;
        this.titleExpression;
    }

    init(config) {
        this.welcomePageId = config.welcomePageId;
        var promises = [];
        var _this = this;
        config.pageIds.forEach(id => { _this.pages[id] = {}; _this.pageData[id] = new Data({}); });
        config.pageIds.forEach(id => { _this.pages[id] = {}; _this.timestamps[id] = {}; });
        config.pageIds.forEach(id => promises.push(_this.loadPageHead(id)));
        config.pageIds.forEach(id => promises.push(_this.loadPageBody(id)));
        config.pageIds.forEach(id => promises.push(_this.loadPageBodyAttributes(id)));
        console.log('PageCOntroller:Promises.all');
        return Promise.all(promises).then(() => _this.findPageId())
            .then(pageId => _this.bindPage(pageId))
            .then(pageId => _this.refreshData(pageId))
            .then(pageId => _this.refreshPage(pageId));
    }


    /**
    * @returns {Promise<string>}
    */
    bindPage(pageId) {
        console.log('bindPage: ' + pageId);
        this.pageId = pageId;
        var _this = this;
        return new Promise((resolve, _) => {
            var headChildArray = _this.pages[pageId].headChildArray;
            var bodyChildArray = _this.pages[pageId].bodyChildArray;
            var attributes = _this.pages[pageId].bodyAttributes;
            this.bindHead(headChildArray);
            this.bindBody(bodyChildArray, attributes);
            console.log('resolve bindPage: ' + pageId);
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
        this.titleExpression = undefined;
    }

    pageAction(pageId, action) {
        var _this = this;
        return this.client.pageAction(pageId, action, this.pageData[pageId]).then(response => {
            var data = _this.pageData[key];
            _this.timestamps
            for (var key of Object.keys(data)) {
                var dataItem = response.data[key];
                data.setValue(key, dataItem.value);
                this.timestamps[key] = dataItem.timestamp;
            }
            return data;
        }).then(controllerId => _this.bindPage(controllerId));
    }


    /**
     * @returns {Promise<string>}
     */
    findPageId() {
        console.log('findPageId');
        var _this = this;
        return new Promise((resolve, _) => {
            var uri = document.location.pathname;
            if (!_this.pages[uri]) {
                uri = _this.welcomePageId;
            }
            console.log('resolve findPageId: ' + uri);
            resolve(uri);
        });
    }

    refresh() {
        var _this = this;
        return this.refreshData().then(() => _this.refreshPage());
    }

    /**
     * @returns {Promise<string>}
     */
    refreshData(pageId) {
        var _this = this;
        console.log('refreshData');
        var data = this.pageData[pageId];
        var timestamps = this.timestamps[pageId];
        var dto = {};
        for (var key of data.getKeys()) {
            var value = data.getValue(key);
            var timestamp = timestamps[key];
            var dataItem = { value: value, timestamp: timestamp };
            dto[key] = dataItem;
        }
        return client.loadPageData(this.pageId, dto).then(response => {
            var responseData = response.data;
            var data = _this.pageData[pageId];
            for (var key of Object.keys(responseData)) {
                var dataItem = responseData[key];
                data.setValue(key, dataItem.value);
                _this.timestamps[key] = dataItem.timestamp;
            }
            console.log('return in refreshData');
            return pageId;
        });
    }

    /**
    * @returns {Promise<string>}
    */
    refreshPage(pageId) {
        console.log('refreshPage: ' + pageId);
        var _this = this;
        return new Promise((resolve, _) => {
            var data = _this.pageData[pageId];
            this.refreshTitle(data);
            _this.head._xis.refresh(data);
            _this.body._xis.refresh(data);
            _this.updateHistory(_this.head, pageId);
            console.log('resolve - refreshPage: ' + pageId);
            resolve(pageId);
        });
    }

    refreshTitle(data) {
        if (this.titleExpression) {
            this.title.innerHTML = this.titleExpression.evaluate(data);
        }
    }

    ignoreElement(element) {
        return element.getAttribute('data-ignore');
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
                this.titleExpression = new TextContentParser(child.innerHTML).parse();
            } else {
                this.head.appendChild(child);
            }
        }
        this.head._xis.updateChildArray();
    }

    /**
     * @param {array<Node>} bodyChildArray
     * @param {attributes} any
     */
    bindBody(bodyChildArray, attributes) {
        for (var name of Object.keys(attributes)) {
            this.body.setAttribute(name, attributes[name]);
        }
        console.log('body-children:' + bodyChildArray.length);
        for (var i = 0; i < bodyChildArray.length; i++) {
            console.log('body-children - append:' + bodyChildArray[i]);
            this.body.appendChild(bodyChildArray[i]);
        }
        this.body._xis.updateChildArray();
    }

}

class Widgets {

    constructor(client) {
        this.widgets = {};
        this.client = client;
    }

    init(config) {
        var _this = this;
        var promises = [];
        config.widgetIds.forEach(id => _this.widgets[id] = {});
        config.widgetIds.forEach(id => promises.push(_this.loadWidget(id)));
        console.log('Widgets - Promise.all');
        return Promise.all(promises).then(() => config);
    }
    /**
    * @returns {Promise<string>}
    */
    loadWidget(widgetId) {
        console.log('loadWidget: ' + widgetId);
        var _this = this;
        return this.client.loadWidget(widgetId).then(widgetHtml => {
            var root = _this.asRootElement(widgetHtml);
            _this.widgets[widgetId].root = root;
            console.log('initialize: ' + root);
            initialize(root);
            return widgetId;
        });
    }

    getWidgetRoot(widgetId) {
        return this.widgets[widgetId].root;
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
}

class ContainerController {

    constructor(client, widgets) {
        this.client = client;
        this.widgets = widgets;
    }

    refresh(containerElement, data) {
        var xis = containerElement._xis;
        var container = xis.container;
        var widgetId = this.getWidgetIdToShow(container, data);
        if (widgetId) {
            if (container.widgetId != widgetId) {
                if (container.widgetId) {
                    this.removeWidget(containerElement, container);
                }
                this.bindWidget(containerElement, container, widgetId);
            }
            var dto = {};
            for (var key of container.data.getKeys()) {
                var value = container.data.getValue(key);
                var timestamp = container.timestamps[key];
                var dataItem = { value: value, timestamp: timestamp };
                dto[key] = dataItem;
            }
            this.client.loadWidgetData(widgetId, dto || {})
                .then(response => {
                    for (var key of Object.keys(response.data)) {
                        var dataItem = response.data[key];
                        container.data.setValue(key, dataItem.value);
                        container.timestamps[key] = dataItem.timestamp;
                    }
                    return container.data;
                }).then(data => xis.refresh(data));


        } else {
            this.removeWidget(containerElement, container);
        }
    }

    getWidgetIdToShow(container, data) {
        return container.expression.evaluate(data);
    }

    removeWidget(containerElement, container) {
        containerElement.removeChild(this.widgetRoot);
        container.widgetId = undefined;
        container.widgetRoot = undefined;
    }

    bindWidget(containerElement, container, widgetId) {
        container.widgetId = widgetId;
        container.widgetRoot = this.widgets.getWidgetRoot(widgetId);
        containerElement.appendChild(container.widgetRoot);
        containerElement._xis.updateChildArray();
    }

}