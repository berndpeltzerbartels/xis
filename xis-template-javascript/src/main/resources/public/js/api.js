
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


class PageController {

    /**
     * @param {Client} client 
     */
    constructor(client) {
        this.client = client;
        this.html = getElementByTagName('html');
        this.head = getElementByTagName('head');
        this.body = getElementByTagName('body');
        this.title = getElementByTagName('title');
        this.html._pageDataMap = {};
        this.html._pageAttributes = {};
        this.pages = {};
        this.pageId = undefined;
        this.welcomePageId;
        this.titleExpression;
    }

    loadPages(config) {
        this.welcomePageId = config.welcomePageId;
        this.html._pageAttributes = config.pageAttributes;
        var promises = [];
        var _this = this;
        config.pageIds.forEach(id => this.pages[id] = {});
        config.pageIds.forEach(id => this.html._pageDataMap[id] = new Data({}));
        config.pageIds.forEach(id => promises.push(_this.loadPageHead(id)));
        config.pageIds.forEach(id => promises.push(_this.loadPageBody(id)));
        config.pageIds.forEach(id => promises.push(_this.loadPageBodyAttributes(id)));
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
        document.pageId = pageId;
        this.pageId = pageId;
        var _this = this;
        return new Promise((resolve, _) => {
            var headChildArray = _this.pages[pageId].headChildArray;
            var bodyChildArray = _this.pages[pageId].bodyChildArray;
            var attributes = _this.pages[pageId].bodyAttributes;
            _this.head._bindChildNodes(headChildArray);
            _this.body._bindBodyAttributes(attributes);
            _this.body._bindChildNodes(bodyChildArray)
            console.log('resolve bindPage: ' + pageId);
            resolve(pageId);
        });
    }


    unbindPage() {
        for (var name of this.body.getAttributeNames()) {
            this.body.removeAttribute(name);
        }
        for (var child of nodeListToArray(this.body.childNodes)) {
            if (!child.getAttribute || !child.getAttribute('data-ignore')) {
                this.body.removeChild(child);
            }
        }
        for (var child of nodeListToArray(this.head.childNodes)) {
            if (!child.getAttribute || !child.getAttribute('data-ignore')) {
                this.head.removeChild(child);
            }
        }
        this.head._childNodes = [];
        this.body._childNodes = [];
        this.titleExpression = '';
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
        var values = {};
        var pageData = this.html._pageDataMap[pageId];
        var pageAttributes = this.html._pageAttributes[pageId];
        if (pageData) {
            for (var dataKey of pageAttributes.modelsToSubmitForModel) {
                values[dataKey] = pageData.getValue([dataKey]);
            }
        }
        return client.loadPageData(this.pageId, values).then(response => {
            _this.html._pageDataMap[pageId] = new Data(response.data);
            return pageId;
        });
    }

    /**
    * @private 
    * @returns {Promise<string>}
    */
    refreshPage(pageId) {
        var _this = this;
        return new Promise((resolve, _) => {
            var data = _this.html._pageDataMap[pageId];
            _this.refreshTitle(data);
            _this.head._refresh(data);
            _this.body._refresh(data);
            _this.updateHistory(pageId);
            console.log('resolve - refreshPage: ' + pageId);
            resolve(pageId);
        });
    }


    refreshTitle(data) {
        if (this.titleExpression) {
            this.title.innerHTML = this.titleExpression.evaluate(data);
        }
    }


    updateHistory(pageId) {
        var title = getElementByTagName('title').innerHTML;
        window.history.replaceState({}, title, pageId);
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
            _this.pages[pageId].headChildArray = nodeListToArray(holder.childNodes);
            initializer.initializeRootElement(holder);
            return pageId;
        });
    }

    /**
    * @private
    * @param {string} pageId
    * @returns {Promise<string>}
    */
    loadPageBody(pageId) {
        var _this = this;
        return this.client.loadPageBody(pageId).then(content => {
            var holder = document.createElement('div');
            holder.innerHTML = content;
            _this.pages[pageId].bodyChildArray = nodeListToArray(holder.childNodes);
            initializer.initializeRootElement(holder);
            return pageId;
        });
    }

    /**
     * @private
     * @param {String} pageId 
     * @returns 
     */
    loadPageBodyAttributes(pageId) {
        var _this = this;
        return this.client.loadPageBodyAttributes(pageId).then(attributes => {
            _this.pages[pageId].bodyAttributes = attributes;
            return pageId;
        });
    }
}


class Widgets {

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
            root._widgetAttributes = _this.widgetAttributes[widgetId];
            root._data = {};
            _this.widgets[widgetId].root = root;
            console.log('initialize: ' + root);
            initializer.initializeRootElement(root);
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

class RootPageInitializer {

    /**
     * 
     * @param {NodeInitializer} nodeInitialier 
     */
    constructor(nodeInitialier) {
        this.nodeInitialier = nodeInitialier;
        this.head = getElementByTagName('head');
        this.body = getElementByTagName('body');
    }

    initialize() {
        this.head._clearChildNodes = function () {
            this._childNodes = [];
            for (var child of nodeListToArray(this.childNodes)) {
                if (child.localName !== 'title' && (!child.getAttribute || child.getAttribute('data-ignore'))) {
                    this.removeChild(child);
                    child._parent = undefined;
                }
            }
        }

        this.body._clearChildNodes = function () {
            this._childNodes = [];
            for (var child of nodeListToArray(this.childNodes)) {
                if (!child.getAttribute || child.getAttribute('data-ignore')) {
                    this.removeChild(child);
                    child._parent = undefined;
                }
            }
        }

        this.head._bindChildNodes = function (headChildArray) {
            this._childNodes = [];
            for (var child of headChildArray) {
                child._parent = this;
                if (child.localName == 'title') {
                    var title = getElementByTagName('title');
                    title.innerHTML = '';
                    title._expression = new TextContentParser(child.innerHTML).parse();
                    title._refresh = function (data) {
                        title.innerHTML = title._expression.evaluate(data);
                    }
                } else {
                    this._childNodes.push(child);
                    this.appendChild(child);
                }
            }
        }

        this.body._bindChildNodes = function (bodyChildArray) {
            this._childNodes = [];
            for (var child of bodyChildArray) {
                child._parent = this;
                this.appendChild(child);
                this._childNodes.push(child);
            }
        }

        this.body._bindBodyAttributes = function (attributes) {
            for (var name of Object.keys(attributes)) {
                this.setAttribute(name, attributes[name]);
            }
        }



        this.head._refresh = function (data) {
            for (var child of this._childNodes) {
                if (child._refresh) {
                    child._refresh(data);
                }
            }
            getElementByTagName('title')._refresh(data);
        }

        this.body._refresh = function (data) {
            for (var child of this._childNodes) {
                if (child._refresh) {
                    child._refresh(data);
                }
            }
        }
    }
}



class NodeInitializer {


    constructor() {
        this.exprParser = new ExpressionParser();
    }

    /**
     * @public
     * @param {Node} node 
     */
    initializeNode(node) {
        if (isElement(node) && !node.getAttribute('data-ignore')) {
            this.initializeElement(node);
        } else {
            this.initializeTextNode(node);
        }
    }

    initializeRootElement(node) {
        this.initializeElement(node);
    }

    initializeElementFlat(element) {
        this.addElementStandardFields(element);
        this.addElementStandardMethods(element);
        this.initializeAttributes(element);
        if (element.getAttribute('data-repeat')) {
            this.initializeRepeat(element);
        }
        if (element.getAttribute('data-widget')) {
            this.initializeWidgetContainer(element);
        }
        if (element.getAttribute('data-widget-link')) {
            this.initializeWidgetLink(element);
        }
        if (element.getAttribute('data-page-link')) {
            this.initializePageLink(element);
        }
    }

    isActionForm(element) {
        return element.localName == 'form' && element.getAttribute('data-action');
    }

    initializeForm(element) {
        var action = element.getAttribute('data-action');
        element.onsubmit = () => false;

    }

    initializeWidgetLink(a) {
        a._widgetLinkExpression = new TextContentParser(a.getAttribute('data-widget-link')).parse();
        a._linkTargetWidgetId = undefined;
        a.href = "#";
        a.onclick = function () {
            var container = domAccessor.getParentWidgetContainer(a);
            container._showWidget(this._linkTargetWidgetId);
        }
    }

    initializePageLink(a) {
        a._pageLinkExpression = new TextContentParser(a.getAttribute('data-page-link')).parse();
        a._linkTargetPageId = undefined;
        a.href = "#";
        a.onclick = function () {
            pageController.unbindPage();
            pageController.bindPage(this._linkTargetPageId)
                .then(pageId => pageController.refreshData(pageId))
                .then(pageId => pageController.refreshPage(pageId))
        }
    }

    /**
    * @private
    * @param {Element} element 
    */
    initializeWidgetContainer(element) {
        element._showWidget = function (widgetId) {
            var widgetRoot = widgets.getWidgetRoot(widgetId);
            if (!this._widgetId || this._widgetId !== widgetId) {
                this._clearChildNodes();
                this.appendChild(widgetRoot);
                this._childNodes = [widgetRoot];
                this._widgetId = widgetId;
                widgetRoot._parent = this;
            }
            var widgetAttributes = widgetRoot._widgetAttributes;
            var values = {};
            var data = widgetRoot._data;
            var widgetAttributes = widgetRoot._widgetAttributes;
            for (var dataKey of widgetAttributes.modelsToSubmitForModel) {
                values[dataKey] = data.getValue([dataKey]);
            }
            client.loadWidgetData(this._widgetId, values)
                .then(response => new Data(response.data))
                .then(data => { widgetRoot._data = data; return data; })
                .then(data => widgetRoot._refresh(data));
        }
        element._refreshWidget = function () {
            var widgetId = this._widgetId ? this._widgetId : this.getAttribute('data-widget');
            this._showWidget(widgetId);
        }
    }



    /**
     * @private
     * @param {Element} element 
     */
    initializeElement(element) {
        this.initializeElementFlat(element);
        this.initializeChildNodes(element);
    }

    /**
     * @private
     * @param {Element} element 
     */
    addElementStandardFields(element) {
        element._childNodes = nodeListToArray(element.childNodes);
        element._parent = element.parentNode;
    }

    /**
     * @private
     * @param {Element} element 
     */
    addElementStandardMethods(element) {
        element._refresh = function (data) {
            this._refreshAttributes(data);
            if (this._widgetLinkExpression) {
                this._linkTargetWidgetId = this._widgetLinkExpression.evaluate(data);
            }
            if (this._pageLinkExpression) {
                this._linkTargetPageId = this._pageLinkExpression.evaluate(data);
            }
            if (this._refreshWidget) {
                this._refreshWidget();
            } else if (this._repeat) {
                refreshRepeat(this, data);
            } else {
                element._refreshChildNodes(data);

            }
        }

        element._refreshAttributes = function (data) {
            for (var attribute of this._attributes) {
                var value = attribute.expression.evaluate(data);
                element.setAttribute(attribute.name, value);
            }
        }

        element._refreshChildNodes = function (data) {
            var childArray = this._childNodes;
            for (let index = 0; index < childArray.length; index++) {
                var child = childArray[index];
                if (!isElement(child) || !child.getAttribute('data-ignore')) {
                    if (child._refresh) {
                        child._refresh(data);
                    }
                }
            }
        }

        element._clearChildNodes = function () {
            for (let index = 0; index < this.childNodes.length; index++) {
                var child = this.childNodes.item(index);
                this.removeChild(child);
            }
        }

    }
    /**
     * @private
     * @param {Element} element 
     */
    initializeRepeat(element) {
        var arr = doSplit(element.getAttribute('data-repeat'), ':');
        var varName = arr[0];
        var arrayExpression = this.exprParser.parse(arr[1]);
        element._repeat = {
            varName: varName,
            arrayExpression: arrayExpression,
            elements: []
        };
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


    initializeAttributes(element) {
        element._attributes = [];
        for (var attrName of element.getAttributeNames()) {
            if (attrName.startsWith('data-')) {
                continue; // otherwise evaluated and will replace logic
                // with static content !
            }
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
    initializeChildNodes(element) {
        for (var i = 0; i < element.childNodes.length; i++) {
            this.initializeNode(element.childNodes[i]);
        }
    }
}


class WidgetController {


}

/**
 * @singelton
 */
class DomAccessor {

    /**
     * 
     * @param {Node} node 
     * @returns 
     */
    getParentWidgetContainer(node) {
        var e = node;
        while (e) {
            if (e.getAttribute('data-widget')) {
                return e;
            }
            e = e._parent;
        }
    }

    getParentData(element) {

    }
}


class UserFunctions {

    /**
     * 
     * @param {DomAccessor} domAccessor 
     */
    constructor(domAccessor) {
        this.domAccessor = domAccessor;
    }

    showWidget(invokerElement, widgetId) {
        invokerElement._showWidget(widgetId);
    }

}

function getElementData(element) {
    if (!element._data) {
        element._data = new Data({});
    }
    return element._data;
}

function refreshRepeat(origElement, parentData) {
    var loopAttributes = origElement._repeat;
    var varName = loopAttributes.varName;
    var dataArr = loopAttributes.arrayExpression.evaluate(parentData);
    if (!dataArr) {
        dataArr = [];
    }
    if (origElement.parentNode) {
        origElement.parentNode.removeChild(origElement);
    }
    var elements = origElement._repeat.elements;
    var i = 0;
    var element = origElement;
    while (i < dataArr.length) {
        if (i >= elements.length) {
            var clone = cloner.cloneNode(element);
            appendSibling(element, clone);
            element = clone;
            elements.push(element);
        } else {
            element = elements[i];
            if (!element.parentNode) {
                element._parent.appendChild(element);
            }
        }
        var value = dataArr[i];
        var data = new Data({}, parentData);
        data.setValue(varName, value);
        element._data = data;
        element._refresh(data);
        i++;
    }
    while (i < elements.length) {
        var element = elements[i];
        if (element.paarentNode) {
            element.paarentNode.removeChild(element);
        }
        i++;
    }
}

function refreshFor(element, loopAttributes, parentData) {
    var varName = loopAttributes.varName;
    var arrPath = loopAttributes.arrPath;
    var dataArr = data.getValue(arrPath);
    if (!dataArr) {
        dataArr = [];
    }
    var nodeArrays = element._forLoop.nodeArrays;
    var origNodeArray = nodeListToArray(element.childNodes);
    var i = 0;
    var e = element;
    while (i < dataArr) {
        var nodeArray;
        if (i >= nodeArrays.length) {
            var clones = cloner.cloneNodeArray(origNodeArray);
            for (var clone of clones) {
                appendSibling(e, clone);
            }
            nodeArray = clone;
        } else {
            nodeArray = nodeArrays[i];
        }
        e = lastArrayElement(nodeArray);
        var value = dataArr[i];
        var data = new Data({}, parentData);
        data.setValue(varName, value);
        for (var node of nodeArray) {
            if (node.refresh) {
                node._data = data;
                node._refresh(data);
            }
        }
        i++;
    }
    while (i < nodeArrays.length) {
        var nodeArray = nodeArrays[i];
        for (var node of nodeArray) {
            if (node.paarentNode) {
                node.paarentNode.removeChild(node);
            }
        }
        i++;
    }
}

function refresh(node, data) {
    if (node.refresh) {
        node.refresh(data);
    }
}



/**
 * 
 * @param {Node} element 
 * @param {Node} sibling 
 */
function appendSibling(element, sibling) {
    var parent = element._parent;
    if (element.nextSibling) {
        parent.insertBefore(sibling, element.nextSibling);
    } else {
        parent.appendChild(sibling);
    }
    sibling._parent = parent;
}


function lastArrayElement(arr) {
    if (arr.length == 0) {
        return undefined;
    }
    return arr[arr.length - 1];
}


function isEmpyArray(arr) {
    return arr.length == 0;
}


class Cloner {

    /**
     * @public
     * @param {Node} node 
     * @returns {Node}
     */
    cloneNode(node) {
        if (isElement(node)) {
            return this.cloneElement(node);
        }
        return this.cloneTextNode(node);
    }

    /**
   * @private
   * @param {Node} element 
   * @returns 
   */
    cloneTextNode(node) {
        if (node._expression) {
            var cloned = document.createTextNode('');
            cloned._expression = node._expression.clone();
            cloned._refresh = function (data) {
                this.nodeValue = this._expression.evaluate(data);
            }
            return cloned;
        }
        return document.createTextNode(node.nodeValue);
    }


    /**
     * @private
     * @param {Element} element 
     * @returns 
     */
    cloneElement(element) {
        var newElement = document.createElement(element.localName);
        this.cloneAttributes(element, newElement);
        this.cloneFrameworkAttributes(element, newElement);
        this.cloneChildNodes(element, newElement);
        this.cloneMethods(element, newElement);
        initializer.initializeElementFlat(newElement);
        return newElement;
    }

    /**
    * @private
    * @param {Element} src 
    * @param {Element} dest 
    */
    cloneMethods(src, dest) {
        dest._refresh = src._refresh;
    }
    /**
    * @private
    * @param {Element} src 
    * @param {Element} dest 
    */
    cloneAttributes(src, dest) {
        for (var name of src.getAttributeNames()) {
            if (name === 'data-repeat' || name == 'data-for') {
                continue;
            }
            var attrValue = src.getAttribute(name);
            dest.setAttribute(name, attrValue);
        }
    }

    /**
     * @private
     * @param {Element} src 
     * @param {Element} dest 
     */
    cloneFrameworkAttributes(src, dest) {
        dest._variableAttributes = src._variableAttributes;
        dest._widgetId = src._widgetId;
    }

    /**
     * Only for childnodes !!!
     * 
     * @private
     * @param {Element} src 
     * @param {Element} dest 
     */
    cloneLoops(src, dest) {
        if (src._repeat) {
            dest._repeat = {
                varName: src._repeat.varName,
                arrayExpression: src._repeat.arrayExpression,
                elements: [] // do not clone
            };
        }
        if (src._for) {
            dest._for = {
                varName: src._for.varName,
                arrayExpression: src._for.arrayExpression,
                elements: [] // do not clone
            };
        }
    }

    /**
     * @private
     * @param {Element} src 
     * @param {Element} dest 
     */
    cloneChildNodes(src, dest) {
        dest._childNodes = [];
        for (var child of src._childNodes) {
            var clonedChild = this.cloneNode(child);
            this.cloneLoops(child, clonedChild); // only for childnodes !
            dest.appendChild(clonedChild);
            dest._childNodes.push(clonedChild);
            clonedChild._parent = dest;
        }
    }

    /**
     * 
     * @param {Array<Node>} nodeArray 
     */
    cloneNodeArray(nodeArray) {
        return nodeArray.map(node => cloneNode(node));
    }
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

function getElementByTagName(name) {
    return document.getElementsByTagName(name).item(0);
}

function empty(str) {
    if (!str) return true;
    if (trim(str).length == 0) return true;
    return false;
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


class Starter {

    /**
     * 
     * @param {PageController} pageController 
     * @param {Widgets} widgets 
     */
    constructor(pageController, widgets, client) {
        this.pageController = pageController;
        this.widgets = widgets;
        this.client = client;
    }

    doStart() {
        var _this = this;
        new RootPageInitializer().initialize();
        this.loadConfig().then(config => _this.widgets.loadWidgets(config))
            .then(config => _this.pageController.loadPages(config));
    }
    /**
    * @returns {Promise<ComponentConfig>}
    */
    loadConfig() {
        return this.client.loadConfig();
    }
}

var client = new Client(new HttpClient());
var widgets = new Widgets(client);
var pageController = new PageController(client);
var cloner = new Cloner();
var initializer = new NodeInitializer();
var domAccessor = new DomAccessor();
var userFunctions = new UserFunctions(domAccessor);
var widgetController = new WidgetController(domAccessor);
var expressionParser = new ExpressionParser();
var starter = new Starter(pageController, widgets, client);
starter.doStart();

console.log('pathname: ' + document.location.pathname);