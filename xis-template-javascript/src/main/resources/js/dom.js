/**
 * A function called with an element as parameter without any return
 * @callback elementConsumer
 * @param {Element} element
 */

/**
 * A function called with a anode as parameter without any return
 * @callback nodeConsumer
 * @param {Element} element
 */

/**
 * A function called with an element as parameter without any return
 * @callback attributeCallback
 * @param {Element}
 */


function showChild(parent, index) {
    var child = parent._xis.childArray[index];
    if (!parent._xis.childVisible[index])
        parent._xis.element.appendChild(child);
    parent._xis.childVisible[index] = true;
    return child;
}

function hideChild(parent, index) {
    var child = parent._xis.childArray[index];
    if (parent._xis.childVisible[index])
        parent._xis.element.removeChild(child);
    parent._xis.childVisible[index] = false;
    return child;
}

function clearChildren(e) {
    var xis = e._xis;
    for (var i = 0; i < xis.childArray.length; i++) {
        hideChild(e, i);
    }
};

function cloneNode(node) {
    if (isElement(node)) {
        return cloneElement(node);
    }
    return cloneTextNode(node);
}

function cloneElement(element) {
    var newElement = document.createElement(element.localName);
    for (var attrName of element.getAttributeNames()) {
        newElement.setAttribute(attrName, element.getAttribute(attrName));
    }
    for (var i = 0; i < element._xis.childArray.length; i++) {
        var child = element._xis.childArray[i];
        var clonedChild = cloneNode(child);
        newElement.appendChild(clonedChild);
    }
    newElement._xis = element._xis.clone(newElement);
    return newElement;
}

function cloneTextNode(textNode) {
    var newNode = document.createTextNode(textNode.nodeValue);
    newNode._xis = textNode._xis;
    return newNode;
}

function evaluateRepeat(e, data) {
    var xis = e._xis;
    var repeat = xis.repeat;
    var parent = xis.parent;
    var arr = repeat.expression.evaluate(data);
    if (!arr) return;
    var valueKey = repeat.varName;
    var newData = new Data({}, data);
    var i = 0;
    var element;
    while (i < arr.length) {
        newData.setValue(valueKey, arr[i]);
        newData.setValue(valueKey + '_index', i);
        if (repeat.cache.elements.length <= i) {
            repeat.cache.elements.push(cloneNode(e));
        }
        element = repeat.cache.elements[i];
        if (!repeat.cache.visible[i]) {
            parent.appendChild(element);
            repeat.cache.visible[i] = true;
        }
        element.childIndex = i;
        refresh(element, newData);
        i++;
    }
    while (i < repeat.cache.elements.length) {
        element = repeat.cache.elements[i];
        if (repeat.cache.visible[i]) {
            parent.removeChild(element);
            repeat.cache.visible[i] = false;
            i++;
        } else {
            break;
        }
    }
}


function refreshAttributes(e, data) {
    var xis = e._xis;
    for (var attrName of Object.keys(xis.attributes)) {
        e.setAttribute(attrName, xis.attributes[attrName].evaluate(data));
    }
}


function setPage(pageId, data) {
    showPage(pageId);
    var root = document.getRootNode();
    refresh(root, data);
}

function refresh(node, data) {
    if (isElement(node)) {
        refreshElement(node, data);
    } else {
        refreshTextNode(node, data);
    }
}

function refreshElement(e, data) {
    var xis = e._xis;
    refreshAttributes(e, data);
    clearChildren(e);
    if (xis.widget) {
        refreshWidget(e, xis);
    }
    for (var i = 0; i < xis.childArray.length; i++) {
        var child = xis.childArray[i];
        if (isElement(child)) {
            if (!child._xis.showHide || child._xis.showHide.evaluate(data)) {
                if (child._xis.repeat) {
                    evaluateRepeat(child, data);
                } else {
                    refreshElement(child, data);
                }
            }
        } else {
            refreshTextNode(child, data);
            showChild(e, i);
        }
    }
}


function refreshTextNode(node, data) {
    node.nodeValue = node._xis.expression.evaluate(data);
}

class Xis {

    constructor(element) {
        this.element = element;
        this.parent = element.parentNode;
        this.attributes = {};
        this.repeat = undefined;
        this.showHide = undefined;
        this.widget = this.widget;
        return this;
    }

    updateChildNodes() {
        this.childArray = toArray(this.element.childNodes);
        this.childVisible = new Array(this.childArray.length);
        return this;
    }

    clone(e) {
        var xis = new Xis(e);
        xis.element = e;
        xis.parent = e.parent;
        xis.childArray = toArray(e.childNodes);
        xis.childVisible = new Array(xis.childArray.length);
        xis.attributes = this.attributes;
        xis.repeat = this.repeat;
        xis.showHide = this.showHide;
        xis.widget = this.widget;
        if (xis.widget)
            xis.widget.currentWidgetId = undefined; // otherwise never laoded
        return xis;
    }
}


function initialize(node) {
    if (isElement(node)) {
        initializeElement(node);
    } else {
        initializeTextNode(node);
    }
}

function initializeElement(element) {
    var xis = new Xis(element);
    element._xis = xis;
    if (element.getAttribute('data-show')) {
        xis.showHide = this.exprParser.parse(element.getAttribute('data-show'));
    }
    if (element.getAttribute('data-repeat')) {
        var arr = doSplit(element.getAttribute('data-repeat'), ':');
        xis.repeat = { expression: new ExpressionParser().parse(arr[1]), varName: arr[0], cache: { elements: [], visible: [] } };
        xis.repeat.cache.elements.push(element);
        xis.repeat.cache.visible.push(true);
    }
    if (element.getAttribute('data-widget')) {
        var attribute = element.getAttribute('data-widget');
        attribute = removeLastChar(attribute);
        var arr = doSplit(element.getAttribute('data-widget'), '(');
        var widgetId = arr[0];
        var parameters = doSplit(arr[1]).map(s => trim(s));
        xis.widget = { widgetId: widgetId, parameters: parameters };
    }
    for (var attrName of element.getAttributeNames()) {
        var attrValue = element.getAttribute(attrName);
        if (attrValue.indexOf('${') != -1) {
            xis.attributes[attrName] = new TextContentParser(attrValue).parse();
        }
    }
    for (var i = 0; i < element.childNodes.length; i++) {
        initialize(element.childNodes.item(i));
    }
    xis.updateChildNodes();
}

function initializeTextNode(node) {
    node._xis = { expression: { evaluate: (data) => node.nodeValue } };
    if (node.nodeValue && node.nodeValue.indexOf('${') != -1) {
        node._xis.expression = new TextContentParser(node.nodeValue).parse();
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
     * 
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

    /**
     * 
     * @param {String} key 
     * @param {any} value 
     */
    setValue(key, value) {
        this.values[key] = value;
    }
}

/**
 * Maps a NodeList to an array.
 *
 * @public
 * @param {NodeList} nodeList
 * @returns {Array}
 */
function toArray(nodeList) {
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


function removeLastChar(string) {
    return string.substring(0, string.length - 1); // surprising, but tested
}


function bindPage(pageId) {
    var xis = html._xis;
    var page = xis.getPage(pageId);
    if (!page) return false;
    var html = getTemplateRoot();
    var head = getTemplateHead();
    var body = getTemplateBody();
    var title = getTitle();
    for (var i = 0; i < page.getHeadElement().childNodes.length; i++) {
        var child = page.getHeadElement().childNodes.item(i);
        if (child.localName && child.localName == title) {
            title.innerHTML = child.innerHTML;
        } else {
            head.appendChild(child);
            xis.head.childNodes.push(child);
        }
    }
    for (var i = 0; i < page.getBodyElement().childNodes.length; i++) {
        body.appendChild(page.getBodyElement().childNodes.item(i));
    }
    for (var name of page.getBodyElement().getAttributeNames()) {
        body.setAttribute(name, page.body.getAttribute(name));
    }
    return true;
}

function unbindPage() {
    var html = getTemplateRoot();
    var xis = html._xis;
    var head = getTemplateHead();
    var body = getTemplateBody();
    var title = getTitle();
    title.innerHTML = '';
    // We do not want to remove our script-tags etc.
    for (var i = 0; i < xis.head.childNodes.length; i++) {
        var child = xis.head.childNodes[i];
        head.removeChild(child);
    }
    for (var i = 0; i < body.childNodes.length; i++) {
        var child = body.childNodes.item(i);
        body.removeChild(child);
    }
    for (var name of body.getAttributeNames()) {
        body.removeAttribute(name);
    }
}

function


function getTemplateHead() {
    return getElementByTagName('head');
}

function getTemplateBody() {
    return getElementByTagName('body');
}

function getTemplateRoot() {
    return document.getRootNode();
}

function getElementByTagName(name) {
    return document.getElementsByTagName(name).item(0);
}



function initPage() {
    var html = document.getElementsByTagName('html').item(0);
    initialize(html);
    refresh(html, new Data({
        title: 'bla', items: [{ name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' },
        { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' },
        { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' },
        { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' },
        { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' },
        { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' },
        { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' },
        { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' },
        { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' },
        { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' },
        { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' },
        { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' },
        { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' },
        { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' },
        { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' },
        { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' },
        { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' },
        { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' },
        { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' },
        { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' },
        { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' },
        { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' }, { name: 'name2' }, { name: 'name1' },
        ],
        countries: [
            { id: 1, name: 'Deutschland' },
            { id: 2, name: 'Spanien' },
            { id: 3, name: 'USA' },
        ],
        selectedCountry: { id: 2, name: 'Spanien' }
    }));
    refresh(html, new Data({
        title: 'bla', items: [{ name: 'name1' }, { name: 'name2' }],
        countries: [
            { id: 1, name: 'Deutschland' },
            { id: 2, name: 'Spanien' },
            { id: 3, name: 'USA' },
        ],
        selectedCountry: { id: 2, name: 'Spanien' }
    }));


}

