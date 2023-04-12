class Initializer {

    /**
     *
     * @param {DomAccessor} domAccessor
     */
    constructor(domAccessor) {
        this.domAccessor = domAccessor;
    }


    initialize(node) {
        if (isElement(node) && !node.getAttribute('ignore')) {
            this.initializeElement(node);
        } else {
            this.initializeTextNode(node);
        }
    }

    initializeElement(element) {
        if (this.isFrameworkElement(element)) {
            this.initializeFrameworkElement(element);
        } else {
            this.initializeHtmlElement(element);
        }
    }

    initializeHtmlElement(element) {
        element._refresh = function (data) {
            for (var attribute of this._attributes) {
                this.setAttribute(atttribute.name, attribute.expression.evaluate(data));
            }
            for (var i = 0; i < this.childNodes; i++) {
                var child = nodeList.item(i);
                if (child._refresh) {
                    child._refresh(data);
                }
            }
        }
    }

    initializeAttributes(element) {
        element._attributes = [];
        for (var attrName of element.getAttributeNames()) {
            var attrValue = element.getAttribute(attrName);
            if (attrValue.indexOf('${') != -1) {
                element._attributes.push({
                    name: attrName,
                    expression: new TextContentParser(attrValue).parse()
                });
            }
        }
    }

    /**
     * @private
     * @param {Element} element
     */
    initializeTextNode(node) {
        if (node.nodeValue && node.nodeValue.indexOf('${') != -1) {
            node._expression = new TextContentParser(node.nodeValue).parse();
            node._refresh = function (data) {
                this.nodeValue = this._expression.evaluate(data);
            }
        }
    }

    initializeFrameworkElement(element) {
        switch (element.localName) {
            case 'xis:foreach':
            case 'xis:forEach':
                element._handler = new ForeachHandler(element);
                break;
            case 'xis:widget-container':
                element._handler = new WidgetContainerHandler(element);
        }
        element._refresh = function (data) {
            this._handler.refresh(data);
        }
    }

    isFrameworElement(element) {
        return element.localName.startsWith('xis:');
    }

    insertForeachAbove(element) {

    }

}


class DomAccessor {

    insertParent(element, elementToInsert) {
       this.replaceElement(element, elementToInsert);
       elementToInsert.appendChild(element);
    }

    replaceElement(old, replacement) {
        var parent = old.parentNode;
        var nextSibling = old.nextSibling;
        parent.removeChild(old);
        if (nextSibling) {
            parent.insertBefore(replacement, nextSibling);
        } else {
            parent.appendChild(replacement);
        }
    }

}
class WidgetService {

    /**
     *
     * @param {Client} client
     */
    constructor(client) {
        this.widgets = {};
        this.client = client;
        this.widgetAttributes = {};
    }

    loadWidgets(config) {
        var _this = this;
        var promises = [];
        this.widgetAttributes = config.widgetAttributes;
        config.widgetIds.forEach(id => _this.widgets[id] = {});
        config.widgetIds.forEach(id => promises.push(_this.loadWidget(id)));
        return Promise.all(promises).then(() => config);
    }
    /**
    * @returns {Promise<string>}
    */
    loadWidget(widgetId) {
        var _this = this;
        return this.client.loadWidget(widgetId).then(widgetHtml => {
            var widget = new Widget();
            widget.id = widgetId;
            widget.root = _this.asRootElement(widgetHtml);
            widget.attributes = _this.widgetAttributes[widgetId];
            _this.widgets[widgetId] = widget;
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
    constructor() {
        this.config = undefined;
        this.clientId = '';
        this.userId = '';
    }

    /**
     * @public
     * @return {Promise<any>}
     */
    loadConfig() {
        return httpClient.get('/xis/config', {})
            .then(content => JSON.parse(content));
    }

    /**
     * @public
     * @param {string} pageId
     * @return {Promise<string>}
     */
    loadPageHead(pageId) {
        return httpClient.get('/xis/page/head', { uri: pageId });
    }


    /**
     * @public
     * @param {string} pageId
     * @return {Promise<string>}
     */
    loadPageBody(pageId) {
        return httpClient.get('/xis/page/body', { uri: pageId });
    }

    /**
     * @public
     * @param {string} pageId
     * @return {Promise<any>}
     */
    loadPageBodyAttributes(pageId) {
        return httpClient.get('/xis/page/body-attributes', { uri: pageId }).then(content => JSON.parse(content));
    }

    /**
    * @public
    * @param {string} pageId
    * @return {Promise<string>}
    */
    loadWidget(widgetId) {
        return httpClient.get('/xis/widget/html/' + widgetId, {});
    }

    /**
     * @public
     * @param {string} pageId
     * @param {Data} data
     * @returns {Promise<any>}
     */
    loadPageData(pageId, data) {
        var request = this.createRequest(pageId, data, undefined);
        return httpClient.post('/xis/page/model', request, {})
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
        return httpClient.post('/xis/widget/model', request)
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
        return httpClient.post('/xis/widget/action', request, {})
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
        return httpClient.post('/xis/page/action', request, {});
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



/**
 * Caching childnodes to avoid unnecessary expensive
 * cloning and initializing of elements and text nodes.
 *
 * @property {array<Node>} nodeArray
 * @property {array<array<Node>>} cache
 */
class NodeCache {

    /**
     * @param {array<Node>} nodeArray
     */
    constructor(nodeArray) {
        this.nodeArray = nodeArray;
        this.cache = [nodeArray];
    }

    /**
     * @public
     * @param {Number} size
     */
    sizeUp(size) {
        while (this.cache.length < size) {
            this.cache.push(this.cloneChildNodes());
        }
    }

    /**
    * @public
    * @param {Number} length
    */
    get length() {
        return this.cache.length;
    }

    /**
     *
     * @param {Number} index
     * @returns {array<Node>}
     */
    getChildren(index) {
        return this.cache[index];
    }

    /**
     * @param {Node} node
     * @returns {array<Node>}
     */
    cloneChildNodes(node) {
        var clones = [];
        for (node of this.nodeArray) {
            clones.push(node.cloneNode);
        }
        return clones;
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
     * @param {Array<string>} path the path of the data value
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


class TagHandler {

    constructor(tag) {
        this.tag = tag;
        this.childArray = this.nodeListToArray(tag.childNodes);
    }

    refresh(data) {
        throw new Error('abstract method');
    }

    clearChildren() {
        for (node of this.childArray) {
            if (node.parentNode) {
                node.parentNode.removeChild(node);
            }
        }
    }

    findParentHtmlElement() {
        var element = this;
        while (element) {
            if (isFrameworkTag(element)) {
                element = element.parentNode;
            } else {
                break;
            }
        }
        return element;
    }


    isFrameworkElement(node) {
        return isElement(node) && node.localName.startsWith('xis:');
    }


    isVisible(node) {
        return node.parentNode != null;
    }

    nodeListToArray(nodeList) {
        var arr = [];
        for (var i = 0; i < nodeList.length; i++) {
            arr.push(nodeList.item(i));
        }
        return arr;
    }

    getAttribute(name) {
        return this.tag.getAttribute(name);
    }

    doSplit(string, separatorChar) {
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
}


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
        this.pageAttributes = config.pageAttributes;
        var _this = this;
        this.findPageForCurrentUrl()
            .then(page => _this.bindPage(page))
            .then(pageId => _this.refreshData(pageId))
            .then(pageId => _this.refreshPage(pageId));
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

    bindBodyAttributes = function (attributes) {
        for (var name of Object.keys(attributes)) {
            this.setAttribute(name, attributes[name]);
        }
    }

    clearBodyAttributes = function () {
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
        for (node of nodeListToArray(element, childNodes)) {
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

class HttpClient {

    /**
     * @param {Function} errorHandler
     */
    constructor(errorHandler) {
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


/**
 * @property {array<string>} pageIds
 * @property {array<string>} widgetIds
 * @property {any} pageHosts
 * @property {any} widgetHosts
 * @property {any} pageAttributes
 * @property {any} widgetAttributes
 */
class Config {

    constructor() {
        this.pageIds = [];
        this.widgetIds = [];
        this.pageHosts = {};
        this.widgetHosts = {};
        this.welcomePageId = undefined;
        this.pageAttributes = {}
        this.widgetAttributes = {};
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
 * @param {Node} node
 * @returns {boolean}
 */
function isElement(node) {
    return node instanceof HTMLElement;
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


class ForeachHandler extends TagHandler {

    /**
     * @param {Element} tag the custom tag '<xis:foreach/>'
     */
    constructor(tag) {
        super(tag);
        this.arrayPath = this.doSplit(this.getAttribute('array'), '.');
        this.varName = this.getAttribute('var');
        this.cache = new NodeCache(this.childArray);
        this.parent = this.findParentHtmlElement();
    }

    /**
     * @public
     * @param {Data} data
     */
    refresh(data) {
        var arr = data.getValue(this.arrayPath);
        this.cache.sizeUp(arr.length);
        for (var i = 0; i < this.cache.length; i++) {
            var subData = new Data({}, data);
            subData.setValue(this.varName, arr[i]);
            var children = this.cache.getChildren(i);
            if (i < arr.length) {
                for (child of children) {
                    if (child.parentNode != this.parent) {
                        this.parent.appendChild(child);
                    }
                    if (child.refresh) {
                        child.refresh(subData);
                    }
                }
            } else if (!child.parentNode) {
                break;
            }
        }
    }
}
class WidgetContainerHandler extends TagHandler {

    /**
     *
     * @param {Element} tag
     * @param {WidgetService} widgetService
     */
    constructor(tag, widgetService) {
        super(tag);
        this.widgetService = widgetService;
        this.parent = this.findParentHtmlElement();
        this.initialWidgetId = this.getAttribute('widget');
        this.widgetRoot;
        this.clearChildren();
    }

    refresh(data) {
        this.ensureWidgetPresent();
        if (this.widgetRoot.refresh) {
            this.widgetRoot.refresh(data);
        }
    }

    ensureWidgetPresent() {
        if (!this.widgetRoot) {
            this.widgetRoot = this.getWidgetRoot(this.widgetId);
            this.showWidget(this.widgetRoot);
        }
    }

    showWidget(widgetRoot) {
        if (this.parent.nextSibling) {
            this.parent.insertBefore(widgetRoot, this.parent.nextSibling);
        } else {
            this.parent.appendChild(widgetRoot);
        }
    }


    getWidgetRoot(widgetId) {
        return this.widgetService.getWidget(widgetId).root;
    }

}
