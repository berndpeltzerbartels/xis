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
    var dataItem = repeat.expression.evaluate(data);
    var arr = dataItem.value;
    var timestamp = dataItem.timestamp;
    if (!arr) return;
    var valueKey = repeat.varName;
    var newData = new Data({}, data);
    var i = 0;
    var element;
    while (i < arr.length) {
        newData.setValue(valueKey, arr[i], timestamp);
        newData.setValue(valueKey + '_index', i, timestamp);
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


function refreshHead(head, data) {

}


function refreshAttributes(e, data) {
    var xis = e._xis;
    for (var attrName of Object.keys(xis.attributes)) {
        e.setAttribute(attrName, xis.attributes[attrName].evaluate(data));
    }
}

function refresh(node, data) {
    if (isElement(node)) {
        refreshElement(node, data);
    } else {
        refreshTextNode(node, data);
    }
}

function refreshContainer(containerElement, data) {
    var xis = containerElement._xis;
    var containerAttributes = xis.container;
    var widgetId = containerAttributes.widgetIdExpr.evaluate(data);
    if (widgetId != containerAttributes.currentWidgetId) {
        var widgetRoot = getRootXis().getWidgetRoot();
        containerElement.appendChild(widgetRoot);
    }
    client.loadData(widgetId, data, newData => {
        newData.parentData = data
        refresh(widgetRoot, newData);
    });
}

function refreshElement(e, data) {
    var xis = e._xis;
    refreshAttributes(e, data);
    clearChildren(e);
    if (xis.container) {
        refreshContainer(e, data);
    } else {
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
}


function refreshTextNode(node, data) {
    if (node._xis && node._xis.expression) {
        node.nodeValue = node._xis.expression.evaluate(data);
    }
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
        for (var i = 0; i < this.childVisible; i++) {
            this.childVisible[i] = true;
        }
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
        xis.container = { widgetIdExpr: new TextContentParser(attribute).parse(), currentWidgetId: undefined, initializedWidgetIds: [] };
    }
    for (var attrName of element.getAttributeNames()) {
        var attrValue = element.getAttribute(attrName);
        if (attrValue.indexOf('${') != -1) {
            xis.attributes[attrName] = new TextContentParser(attrValue).parse();
        }
    }
    for (var i = 0; i < element.childNodes.length; i++) {
        var child = element.childNodes.item(i);
        initialize(child);
    }
    xis.updateChildNodes();
}

function initializeTextNode(node) {
    node._xis = { expression: { evaluate: (_) => node.nodeValue } };
    if (node.nodeValue && node.nodeValue.indexOf('${') != -1) {
        node._xis.expression = new TextContentParser(node.nodeValue).parse();
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

function getTemplateHead() {
    return getElementByTagName('head');
}

function getTemplateTitle() {
    return getElementByTagName('title');
}


function getTemplateBody() {
    return getElementByTagName('body');
}

function getTemplateRoot() {
    return document.getRootNode();
}
function getRootXis() {
    getTemplateRoot()._xis;
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

