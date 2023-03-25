
class LoopAttributes {

    constructor() {
        this.valueKey = undefined;
        this.arrayExpression = [];
    }

    clone() {
        var attributes = new LoopAttributes();
        attributes.valueKey = this.valueKey;
        attributes.arrayExpression = this.arrayExpression;
        return attributes;
    }
}


class VariableAttribute {

    constructor() {
        this.name = '';
        this.expression = undefined;
    }

    clone() {
        var attributes = new VariableAttribute();
        attributes.name = this.name;
        attributes.expression = this.expression;
        return attributes;
    }

    evaluateAttributeValue(data) {
        return this.expression.evaluate(data);
    }
}

class ContainerAttributes {

    constructor(widgetIdExpression) {
        this.widgetIdExpression = widgetIdExpression;
    }

    clone() {
        var attributes = new ContainerAttributes();
        attributes.widgetIdExpression = this.widgetIdExpression;
        return attributes;
    }

    evaluateWidgetId(data) {
        return this.widgetIdExpression.evaluate(data);
    }
}

class ElementAttributes {

    constructor() {
        this.repeatLoopAttributes = undefined;
        this.forLoopAttributes = undefined;
        this.variableAttributes = [];
        this.showExpression = undefined;
        this.hideExpression = undefined;
        this.containerAttributes = undefined;
    }

    clone() {
        var attributes = new ElementAttributes();
        attributes.repeatLoopAttributes = this.repeatLoopAttributes;
        attributes.forLoopAttributes = this.forLoopAttributes;
        attributes.variableAttributes = this.variableAttributes;
        attributes.showExpression = this.showExpression;
        attributes.containerAttributes = this.containerAttributes;
    }
}



/**
 * Hierarchical page-data
 */
class Data {

    /**
     * 
     * @param {any} values 
     */
    constructor(values, parentData = undefined) {
        this.values = values;
        this.parentData = parentData;
    }
    /**
     * @public 
     * @param {Array} path the path of the data value
     * @returns {any}
     */
    getValue(path) {
        var dataNode = this.values;
        for (var i = 0; i < path.length; i++) {
            var key = path[i];
            if (dataNode[key]) {
                dataNode = dataNode[key];
            } else {
                dataNode = undefined;
                break;
            }
        }
        if (dataNode === undefined && this.parentData) {
            return this.parentData.getValue(path)
        }
        return dataNode;
    }

    getKeys() {
        return Object.keys(this.values);
    }

    /**
     * @public 
     * @param {String} key 
     * @param {any} value 
     */
    setValue(key, value) {
        this.values[key] = value;
    }
}

class DataItem {

    constructor() {
        this.key = '';
        this.value = undefined;
        this.timestamp = -1;
    }

    getValue(path) {
        var dataNode = this.values;
        for (var i = 1; i < path.length; i++) {
            var key = path[i];
            if (dataNode[key]) {
                dataNode = dataNode[key].value;
            } else {
                dataNode = undefined;
                break;
            }
        }
        return dataNode;
    }

}


/**
 * @property {any} data
 * @property {string} clientId
 * @property {string} userId
 * @property {string} key // action-key or model-key
 * @property {string} controllerId
 * @property {string} type
 */
class ComponentRequest {

    constructor() {
        this.data = {};
        this.clientId = '';
        this.userId = '';
        this.key = '';
        this.controllerId = '';
        this.type = '';
    }
}



/**
 * @property {array<string>} pageIds
 * @property {array<string>} widgetIds
 * @property {any} pageHosts
 * @property {any} widgetHosts
 * @property {string} welcomePageId
 */
class Config {

    constructor() {
        this.pageIds = [];
        this.widgetIds = [];
        this.pageHosts = {};
        this.widgetHosts = {};
        this.welcomePageId = undefined;
    }

    /**
     * @public
     * @param {string} id 
     * @returns {string}
     */
    getPageHost(id) {
        return this.pageHosts[id];
    }

    /**
     * @public
     * @param {string} id 
     * @returns {string} 
     */
    getWidgetHost(id) {
        return this.widgetHosts[id];
    }
}


class NodeCloner {

    clone(node) {
        if (isElement(node)) {
            return this.cloneElement(node);
        }
        return this.cloneTextNode(node);
    }

    cloneElement(element) {
        var newElement = document.createElement(element.localName);
        for (var attrName of element.getAttributeNames()) {
            newElement.setAttribute(attrName, element.getAttribute(attrName));
        }
        newElement._elementAttributes = element._elementAttributes;
    }

    cloneChildNodes(newElement, srcElement) {
        newElement._childNodes = srcElement._childNodes.clone();
    }

    cloneTextNode(node) {
        if (node._expression) {
            var cloned = document.createTextNode('');
            cloned._expression = node._expression;
            return cloned;
        }
        return document.createTextNode(node.nodeValue);
    }
}


class ChildNodes {

    /**
     * 
     * @param {ElementController} element 
     * @param {NodeCloner} nodeCloner 
     */
    constructor(element, nodeCloner) {
        this.element = element;
        this.nodeCloner = nodeCloner;
        this._childArray = []; // never changes
    }

    get childArray() {
        return this._childArray;
    }

    init() {
        this._childArray = nodeListToArray(element.childNodes);
    }

    hideAll() {
        for (var i = 0; i < this._childArray.length; i++) {
            var child = this._childArray[i];
            if (child.parent) {
                this.element.removeChild(this._childArray[i]);
            }
        }
    }

    showAll() {
        for (var i = this._childArray.length - 1; i > -1; i--) {
            var child = this._childArray[i];
            if (!child.parent) {
                this.showChild(i);
            }
        }
    }

    showChild(index) {
        for (var i = index + 1; i < this._childArray.length; i++) {
            if (!this.childHidden[i]) {
                this.element.insertBefore(this._childArray[index], this._childArray[i]);
            }
        }
    }

    isVisible(node) {
        return this._childArray[this.childIndex(node)].parent !== undefined;
    }

    childIndex(node) {
        for (var i = 0; i < this._childArray.length; i++) {
            if (this._childArray[i] == node) {
                return i;
            }
        }
        return -1;
    }

    clone() {
        var cloned = new ChildNodes();
        for (var child of this._childArray) {
            var clone = this.cloneNode(child);
            if (child.parent) {
                child.parent.appendChild(clone);
            }
            clone.childArray.push(clone);
        }
        return cloned;
    }

    cloneNode(node) {
        return this.nodeCloner.clone(node);
    }
}

class ForLoopCache {
    /**
     * 
     * @param {Element} element 
     */
    constructor(element) {
        this.childNodes = element._childNodes;
        this.rows = [];
        this.maxVisibleIndex = -1;
    }

    reset() {
        this.maxVisibleIndex = -1;
    }

    /**
     * 
     * @param {ChildNodes} index 
     * @returns 
     */
    increaseAndGet(index) {
        this.maxVisibleIndex = index;
        while (this.rows < index) {
            var childNodes = this.childNodes.clone();
            this.rows.push(childNodes);
        }
        return childNodes[index];
    }

    hideNonVisible() {
        for (var i = this.maxVisibleIndex + 1; i < this.rows.length; i++) {
            var childNodes = this.rows[i];
            childNodes.hideAll();
        }
    }
}


class RepeatLoopCache {

    constructor(rowElement) {
        this.rowElement = rowElement;
        this.maxVisibleIndex = -1;
        this.cachedRowElements = [];
        this.nodeCloner = new NodeCloner();
    }

    reset() {
        this.maxVisibleIndex = -1;
    }

    increaseAndGet(index) {
        while (this.cachedRowElements.length < index) {
            this.cachedRowElements.push(this.nodeCloner.cloneElement(this.rowElement));
            this.maxVisibleIndex = index;
        }
        return this.cachedRowElements[index];
    }

    hideNonVisible() {
        for (var i = this.maxVisibleIndex + 1; i < this.cachedRowElements.length; i++) {
            var element = this.cachedRowElements[i];
            if (element.parent) {
                parent.removeChild(element);
            }
        }
    }
}

class RepeatLoopRefresher {

    /**
     * 
     * @param {TreeRefresher} treeRefresher 
     */
    constructor(treeRefresher) {
        this.treeRefresher = treeRefresher;
    }

    /**
     * 
     * @param {LoopAttributes} loopAttributes 
     * @param {Data} data 
     */
    refreshLoop(parent, loopAttributes, data) {
        var valueKey = this.getKey(loopAttributes);
        var arr = loopAttributes.arrayExpression.evaluate(data);
        var newData = new Data({}, data);
        var index = 0;
        var cache = this.getCache(parent);
        while (index < arr.length) {
            newData.setValue(valueKey, arr[i]);
            newData.setValue(valueKey + '_index', i);
            var rowElement = cache.increaseAndGet(i++);
            if (!rowElement.parent) {
                parent.appendChild(rowElement);
            }
            this.treeRefresher.refresh(rowElement, data);
        }
        cache.hideNonVisible();
    }

    getCache(element) {
        var cache = element._repeatCache;
        if (!cache) {
            cache = new RepeatLoopCache(element);
            element._repeatCache = cache;
        }
        cache.reset();
        return cache;
    }
}

class ForLoopRefresher {

    /**
    * @param {TreeRefresher} treeRefresher 
    */
    constructor(treeRefresher) {
        this.treeRefresher = treeRefresher;
    }

    /**
     * @param {Element} element
     * @param {LoopAttributes} loopAttributes 
     * @param {Data} data 
     */
    refreshLoop(element, loopAttributes, data) {
        var valueKey = this.getKey(loopAttributes);
        var arr = loopAttributes.arrayExpression.evaluate(data);
        var newData = new Data({}, data);
        var index = 0;
        var cache = this.getCache(element);
        while (index < arr.length) {
            newData.setValue(valueKey, arr[i]);
            newData.setValue(valueKey + '_index', i);
            var childNodes = cache.increaseAndGet(i++);
            childNodes.showAll();
        }
        cache.hideNonVisible();
    }

    getCache(element) {
        var cache = element._repeatCache;
        if (!cache) {
            cache = new ForLoopCache(element);
            element._repeatCache = cache;
        }
        cache.reset();
        return cache;
    }
}



class TreeRefresher {

    /**
     * 
     * @param {Client} client 
     * @param {Widgets} widgets
     */
    constructor(client, widgets) {
        this.repeatRefresher = new RepeatLoopRefresher(this);
        this.forRefresher = new ForLoopRefresher(this);
        this.widgetContainerRefresher = new WidgetContainerRefresher(client, widgets);
    }

    refreshRoot(head, data) {
        for (var child of nodeListToArray(head.childNodes)) {
            if (!child.getAttribute || !child.getAttribute('data-ignore')) {
                this.refresh(child, data);
            }
        }
    }

    refresh(node, data) {
        if (isElement(node)) {
            this.refreshElement(node, data);
        } else if (node._expression) {
            this.refreshTextNode(node, node._expression, data);
        }
    }

    refreshElement(element, data) {
        var elementAttributes = element._elementAttributes;
        if (!this.refreshShow(element, elementAttributes, data)) {
            return;
        }
        if (!this.refreshHide(element, elementAttributes, data)) {
            return;
        }
        this.refreshAttributes(element, elementAttributes, data);
        if (elementAttributes.forLoopAttributes) {
            this.forRefresher.refresh(element, elementAttributes.forLoopAttributes, data);
        } else if (elementAttributes.repeatLoopAttributes) {
            this.repeatRefresher.refresh(element, elementAttributes.repeatLoopAttributes, data);
        } else if (elementAttributes.containerAttributes) {
            this.refreshContainer(element, elementAttributes.containerAttributes);
        } else {
            this.refreshChildNodes(element, data);
        }
    }

    refreshChildNodes(element, data) {
        var childNodes = element._childNodes;
        for (var child of childNodes.childArray) {
            this.refresh(child, data);
        }
    }

    refreshTextNode(node, expression, data) {
        node.nodeValue = expression.evaluate(data);
    }

    refreshShow(element, elementAttributes, data) {
        var visible = true;
        if (elementAttributes.showExpression) {
            visible = elementAttributes.showExpression.evaluate(data);
            this.updateVisibility(element, visible);
        }
        return visible;
    }

    refreshHide(element, elementAttributes, data) {
        var visible = true;
        if (elementAttributes.hideExpression) {
            visible = !elementAttributes.hideExpression.evaluate(data);
            this.updateVisibility(element, visible);
        }
        return visible;
    }

    refreshAttributes(element, elementAttributes, data) {
        for (attribute of elementAttributes.variableAttributes) {
            var value = attribute.expression.evaluate(data);
            element.setAttribute(attribute.name, value);
        }
    }

    updateVisibility(element, visible) {
        var parentChildNodes = element._parent._childNodes;
        if (visible) {
            parentChildNodes.showChild(element._childIndex);
        } else {
            parentChildNodes.hideChild(element._childIndex);
        }
    }
}


class Initializer {

    /**
     * 
     * @param {NodeCloner} nodeCloner 
     */
    constructor(nodeCloner) {
        this.nodeCloner = nodeCloner;
        this.exprParser = new ExpressionParser();
    }

    initialize(element) {
        this.initializeElement(element);
    }

    initializeChildren(element) {
        var childNodes = nodeListToArray(element.childNodes); // We need a copy here to allow remove nodes
        for (var i = 0; i < childNodes.length; i++) {
            var node = childNodes[i];
            node._parent = element;
            if (isElement(node) && !node.getAttribute('data-ignore')) {
                this.initializeElement(node);
            } else {
                this.initializeTextNode(node);
            }
        }
    }

    initializeElement(element) {
        element._elementAttributes = new ElementAttributes();
        element._childNodes = new ChildNodes(element, this.nodeCloner);
        if (element.getAttribute('data-ignore')) {
            return;
        }
        if (element.getAttribute('data-widget')) {
            this.initializeWidgetContainer(element);
        }
        if (element.getAttribute('data-show')) {
            this.initializeShowAttribute(element, xis, this.exprParser);
        }
        if (element.getAttribute('data-repeat')) {
            this.initializeRepeat(element, this.exprParser);
        }
        this.initializeAttributes(element);
        this.initializeChildren(element); // otherwise already initialized
    }

    initializeRepeat(element, exprParser) {
        var arr = doSplit(element.getAttribute('data-repeat'), ':');
        var loopAttributes = new LoopAttributes();
        loopAttributes.valueKey = arr[0];
        loopAttributes.arrayExpression = exprParser.parse(arr[1]);
        element._elementAttributes.repeatLoopAttributes = loopAttributes;
        element._repeatCache = new RepeatLoopCache(element);
    }

    initializeShowAttribute(element, exprParser) {
        element._elementAttributes.showExpression = exprParser.parse(element.getAttribute('data-show'));
    }

    initializeWidgetContainer(element) {
        element._elementAttributes.widgetIdExpression = new TextContentParser(element.getAttribute('data-widget')).parse();
    }

    initializeAttributes(element) {
        for (var attrName of element.getAttributeNames()) {
            var attrValue = element.getAttribute(attrName);
            if (attrValue.indexOf('${') != -1) {
                var variableAttribute = new VariableAttribute();
                variableAttribute.name = attrName;
                variableAttribute.expression = new TextContentParser(attrValue).parse();
                element._elementAttributes.variableAttributes.push(variableAttributes);
            }
        }
    }

    initializeTextNode(node) {
        if (empty(node.nodeValue)) {
            node.parentNode.removeChild(node);
        } else if (node.nodeValue && node.nodeValue.indexOf('${') != -1) {
            node._expression = new TextContentParser(node.nodeValue).parse();
        }
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

class WidgetContainerRefresher {

    constructor(client, widgets) {
        this.client = client;
        this.widgets = widgets;
    }

    refresh(containerElement, containerAttributes, data) {
        var widgetId = containerAttributes.evaluateWidgetid(data);
        if (widgetId) {
            if (containerElement._widgetId != widgetId) {
                if (containerElement._widgetId) {
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
                }).then(data => elementController.refresh(data));


        } else {
            this.removeWidget(containerElement, container);
        }
    }

    removeWidget(containerElement) {
        containerElement.removeChild(this.widgetRoot);
        containerElement._widgetId = undefined;
        containerElement._widgetRoot = undefined;
    }

    bindWidget(containerElement, widgetId) {
        containerElement._widgetId = widgetId;
        containerElement._widgetRoot = this.widgets.getWidgetRoot(widgetId);
        containerElement.appendChild(container.widgetRoot);
    }

}

class PageController {

    /**
     * @param {TreeRefresher} treeRefresher
     * @param {Client} client 
     */
    constructor(treeRefresher, client) {
        this.treeRefresher = treeRefresher;
        this.client = client;
        this.html = this.getElementByTagName('html');
        this.head = this.getElementByTagName('head');
        this.body = this.getElementByTagName('body');
        this.title = this.getElementByTagName('title');
        this.head._xis = {};
        this.body._xis = {};
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
            _this.treeRefresher.refreshRoot(_this.head, data);
            _this.treeRefresher.refreshRoot(_this.body, data);
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
            _this.pages[pageId].headChildArray = nodeListToArray(holder.childNodes);
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
            _this.pages[pageId].bodyChildArray = nodeListToArray(holder.childNodes);
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
    }

}

/**
 * @property {HttpClient} httpClient
 * @property {Config} config
 * @property {string} clientId
 * @property {string} userId
 */
class Client {

    /**
     * 
     * @param {HttpClient} httpClient 
     */
    constructor(httpClient) {
        this.httpClient = httpClient;
        this.config = undefined;
        this.clientId = '';
        this.userId = '';
    }

    /**
     * @public
     * @return {Promise<any>} 
     */
    loadConfig() {
        return this.httpClient.get('/xis/config', {})
            .then(content => JSON.parse(content));
    }

    /**
     * @public
     * @param {string} pageId
     * @return {Promise<string>} 
     */
    loadPageHead(pageId) {
        return this.httpClient.get('/xis/page/head', { uri: pageId });
    }


    /**
     * @public
     * @param {string} pageId
     * @return {Promise<string>} 
     */
    loadPageBody(pageId) {
        return this.httpClient.get('/xis/page/body', { uri: pageId });
    }

    /**
     * @public
     * @param {string} pageId
     * @return {Promise<any>} 
     */
    loadPageBodyAttributes(pageId) {
        return this.httpClient.get('/xis/page/body-attributes', { uri: pageId }).then(content => JSON.parse(content));
    }

    /**
    * @public
    * @param {string} pageId
    * @return {Promise<string>} 
    */
    loadWidget(widgetId) {
        return this.httpClient.get('/xis/widget/html/' + widgetId, {});
    }

    /**
     * @public
     * @param {string} pageId
     * @param {Data} data
     * @returns {Promise<any>}
     */
    loadPageData(pageId, data) {
        var request = this.createRequest(pageId, data, undefined);
        return this.httpClient.post('/xis/page/model', request, {})
            .then(content => JSON.parse(content));
    }

    /**
     * @public
     * @param {string} widgetId
     * @param {Data} data
     * @returns {Promise<any>}
     */
    loadWidgetData(widgetId, data) {
        var request = this.createRequest(widgetId, data, undefined);
        return this.httpClient.post('/xis/widget/model', request)
            .then(content => JSON.parse(content));
    }

    /**
     * @public
     * @param {string} widgetId
     * @param {string} action 
     * @param {Data} data
     * @returns {Promise<any>}
     */
    widgetAction(widgetId, action, data) {
        var request = this.createRequest(widgetId, data, action);
        return this.httpClient.post('/xis/widget/action', request, {})
            .then(content => JSON.parse(content));
    }

    /**
     * @public
     * @param {string} pageId
     * @param {string} action 
     * @param {Data} data
     * @returns {Promise<any>}
     */
    pageAction(pageId, action, data) {
        var request = this.createRequest(pageId, data, action);
        return this.httpClient.post('/xis/page/action', request, {});
    }


    /**
    * @private
    * @param {string} controllerId 
    * @param {any} data 
    */
    createRequest(controllerId, data, action) {
        var request = new ComponentRequest();
        request.clientId = this.clientId;
        request.userId = this.userId;
        request.controllerId = controllerId;
        request.action = action;
        request.data = data;
        return request;
    }
}


class HttpClient {

    /**
     * @param {Function} errorHandler 
     */
    constructor(errorHandler) {
        this.className = 'HttpClient';
        this.errorHandler = errorHandler;
    }

    /**
     * @public
     * @param {string} uri
     * @param {any} payload 
     * @return {Promise<any>}
     * 
     */
    post(uri, payload, headers) {
        if (!headers) headers = {};
        var payloadJson = JSON.stringify(payload);
        headers['Content-type'] = 'application/json';
        return this.doRequest(uri, headers, 'POST', payloadJson);
    }

    /**
     * @public
     * @param {string} uri 
     * @param {any} headers
     * @return {Promise<any>}
     */
    get(uri, headers) {
        return this.doRequest(uri, headers, 'GET', undefined);
    }

    /**
     * @private
     * @param {string} uri 
     * @param {any} headers
     * @param {string} method 
     * @param {any} payload 
     * @return {Promise<any>}
     */
    doRequest(uri, headers, method, payload) {
        var xmlHttp = new XMLHttpRequest();
        xmlHttp.open(method, uri, true); // true for asynchronous
        for (var name of Object.keys(headers)) {
            xmlHttp.setRequestHeader(name, headers[name]);
        }
        var promise = new Promise((resolve, reject) => {
            xmlHttp.onreadystatechange = function () {
                // TODO Handle errors and "304 NOT MODIFIED"
                // TODO Add headers to allow 304
                // Readystaet == 4 for 304 ?
                if (xmlHttp.readyState == 4) { // TODO In Java 204 if there is no server-method
                    if (xmlHttp.status == 200) {
                        resolve(xmlHttp.responseText);
                    } else {
                        reject('status: ' + xmlHttp.status);
                    }
                }
                // TODO use errorhandler
            }

        });

        if (payload) {
            xmlHttp.send(payload);
        }
        else {
            xmlHttp.send();
        }
        return promise;
    }

}


function refresh(node, data) {
    var refresher = new TreeRefresher();
    refresher.refresh(node, data);
}



function initialize(element) {
    var initializer = new Initializer();
    initializer.initialize(element);
}

/**
 * Maps a NodeList to an array.
 *
 * @public
 * @param {NodeList} nodeList
 * @returns {Array}
 */
function nodeListToArray(nodeList) {
    var arr = [];
    for (var i = 0; i < nodeList.length; i++) {
        arr.push(nodeList.item(i));
    }
    return arr;
}

/** 
 * @param {Node} node 
 * @returns {boolean}
 */
function isElement(node) {
    return node instanceof HTMLElement;
}


/**
 * 
 * @param {String} string 
 * @param {String} separatorChar 
 * @returns 
 */
function doSplit(string, separatorChar) {
    var rv = [];
    var buffer = '';
    for (var i = 0; i < string.length; i++) {
        var c = string.charAt(i);
        if (c === separatorChar) {
            rv.push(buffer);
            buffer = '';
        } else {
            buffer += c;
        }
    }
    rv.push(buffer);
    return rv;
}

function empty(str) {
    if (!str) return true;
    if (trim(str).length == 0) return true;
    return false;
}

function appendArray(arr1, arr2) {
    var arr = [];
    for (var e of arr1) {
        arr.push(e);
    }
    for (var e of arr2) {
        arr.push(e);
    }
    return arr;
}


function getElementByTagName(name) {
    return document.getElementsByTagName(name).item(0);
}


