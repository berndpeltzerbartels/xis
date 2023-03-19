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
    if (parent._xis.childVisible[index]) {
        console.log('remove child');
        parent._xis.element.removeChild(child);
    }
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

function refreshRepeat(e, data) {
    var xis = e._xis;
    var repeat = xis.repeat;
    var parent = xis.parent;
    var dataItem = repeat.expression.evaluate(data);
    if (!dataItem) return;
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
            console.log('remove child');
            repeat.cache.visible[i] = false;
            i++;
        } else {
            break;
        }
    }
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
    xis.refresh(data);
}


function refreshTextNode(node, data) {
    if (node._xis && node._xis.expression) {
        node.nodeValue = node._xis.expression.evaluate(data);
    }
}

class XisTextNode {
    constructor(node) {
        this.node = node;
        this.expression = new TextContentParser(node.nodeValue).parse();
    }

    refresh(data) {
        this.node.nodeValue = this.expression.evaluate(data);
    }
}

class XisElement {

    constructor(element) {
        this.element = element;
        this.parent = element.parentNode;
        this.attributeExpressions = {};
        this.repeat = undefined;
        this.showHide = undefined;
        this.widget = undefined;
        this.childArray = toArray(this.element.childNodes);
        this.childVisible = new Array(this.childArray.length);
        return this;
    }

    update() {
        this.childArray = toArray(this.element.childNodes);
        this.childVisible = new Array(this.childArray.length);
    }

    initialize() {
        for (var i = 0; i < this.childArray.length; i++) {
            var node = this.childArray[i];
            if (isElement(node) && !node.getAttribute('data-ignore')) {
                this.initializeElement(node);
            } else {
                this.initializeTextNode(node);
            }
            this.childVisible[i] = true;
        }
    }

    initializeElement(element) {
        var xis = new XisElement(element);
        element._xis = xis;
        if (element.getAttribute('data-widget')) {
            xis.container = { expression: new TextContentParser(element.getAttribute('data-widget')).parse(), data: {} };
        }
        if (element.getAttribute('data-show')) {
            xis.showHide = this.exprParser.parse(element.getAttribute('data-show'));
        } if (element.getAttribute('data-repeat')) {
            var arr = doSplit(element.getAttribute('data-repeat'), ':');
            xis.repeat = { expression: new ExpressionParser().parse(arr[1]), varName: arr[0], cache: { elements: [], visible: [] } };
            xis.repeat.cache.elements.push(element);
            xis.repeat.cache.visible.push(true);
        }
        for (var attrName of element.getAttributeNames()) {
            var attrValue = element.getAttribute(attrName);
            if (attrValue.indexOf('${') != -1) {
                xis.attributes[attrName] = new TextContentParser(attrValue).parse();
            }
        }
        if (!xis.container) {
            xis.initialize(); // otherwise already initialized
        }

    }


    initializeTextNode(node) {
        if (empty(node.nodeValue)) {
            node.parentNode.removeChild(node);
        } else if (node.nodeValue && node.nodeValue.indexOf('${') != -1) {
            node._xis = new XisTextNode(node);
        }
    }


    refresh(data) {
        for (var i = 0; i < this.childArray.length; i++) {
            var child = this.childArray[i];
            var xis = child._xis;
            if (!xis) {
                continue;
            }
            var showChild = !this.showHide || this.showHide.evaluate(data);
            if (showChild && !this.isVisibleChild(i)) {
                this.show(i);
            } else if (!showChild && this.isVisibleChild(i)) {
                this.hide(i);
            }
            if (xis.container) {
                containerController.refresh(child, data);
            }
            if (xis.repeat) {
                refreshRepeat(child, data);
            }
            xis.refresh(data);
        }
    }

    refreshAttributes(data) {
        for (var attrName of Object.keys(this.attributes)) {
            e.setAttribute(attrName, this.attributes[attrName].evaluate(data));
        }
    }

    hide(index) {
        console.log('remove child');
        this.element.removeChild(this.childArray[index]);
        this.childVisible[index] = false;
    }

    show(index) {
        var child = this.childArray[index];
        var next = undefined;
        for (var i = index + 1; i < this.childVisible.length; i++) {
            if (this.childVisible[i]) {
                next = this.childArray[i];
                break;
            }
        }
        if (next) {
            this.element.insertBefore(child, next);
        } else {
            this.element.appendChild(child);
        }
        this.childVisible[i] = true;
    }

    isVisibleChild(index) {
        return this.childVisible[index];
    }



    clone(e) {
        var xis = new XisElement(e);
        xis.element = e;
        xis.parent = e.parent;
        xis.childArray = toArray(e.childNodes);
        xis.childVisible = new Array(xis.childArray.length);
        xis.attributeExpressions = this.attributeExpressions;
        xis.repeat = this.repeat;
        xis.showHide = this.showHide;
        xis.container = this.container;
        return xis;
    }
}

function initialize(element) {
    element._xis = new XisElement(element);
    element._xis.initialize();
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

function empty(str) {
    if (!str) return true;
    if (!trim(str).length == 0) return true;
    return false;
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

