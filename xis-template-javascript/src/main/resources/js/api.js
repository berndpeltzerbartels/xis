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
    for (var i = 0; i < arr.length; i++) {
        newData.setValue(valueKey, arr[i]);
        newData.setValue(valueKey + '_index', i);
        var element;
        if (repeat.cache.length <= i) {
            repeat.cache.push(cloneNode(e));
        }
        element = repeat.cache[i];
        element.childIndex = i;
        parent.appendChild(element);
        refresh(element, newData);
    }
}


function refreshAttributes(e, data) {
    var xis = e._xis;
    for (var attrName of Object.keys(xis.attributes)) {
        e.setAttribute(attrName, xis.attributes[attrName].evaluate(data));
    }
}

function refreshDocument(root, data) {
    var xis = root._xis;
    for (var i = 0; i < xis.childArray.length; i++) {
        refresh(xis.childArray[i], data);
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
    refreshAttributes(e, data);
    clearChildren(e);
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
        xis.repeat = { expression: new ExpressionParser().parse(arr[1]), varName: arr[0], cache: [] };
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
 * Util-class to navigate among
 * a string's characters.
 */
class CharIterator {

    /**
     * 
     * @param {string} src 
     */
    constructor(src) {
        this.src = src;
        this.index = -1;
    }

    /**
     * @public
     * @returns {boolean}
     */
    hasNext() {
        return this.index + 1 < this.src.length;
    }

    /**
     * @public
     * @returns {any}
     */
    current() {
        return this.src[this.index];
    }

    /**
     * @public
     * @returns {any}
     */
    next() {
        this.index++;
        return this.src[this.index];
    }

    /**
     * @public
     * @returns {any}
     */
    beforeCurrent() {
        return this.index - 1 > -1 ? this.src[this.index - 1] : undefined;
    }

    /**
     * @public
     * @returns {any}
     */
    afterCurrent() {
        return this.index + 1 < this.src.length ? this.src[this.index + 1] : undefined;
    }

}


/**
 * Represents text containg static string parts
 * and variables like "My name is ${name}"
 */
class TextContent {

    constructor(parts) {
        this.parts = parts;
    }

    /**
     * @public
     * @param {Data} data 
     * @returns the string we get after replacing the 
     * vriables with the actual data.
     */
    evaluate(data) {
        return this.parts.map(part => part.asString(data)).reduce((s1, s2) => s1 + s2);
    }

}

class TextContentParser {

    constructor(src) {
        this.chars = new CharIterator(src);
        this.parts = [];
    }

    parse() {
        this.readText();
        return new TextContent(this.parts);
    }


    readText() {
        var buff = '';
        while (this.chars.hasNext()) {
            var currentChar = this.chars.next();
            if (currentChar == '$' && this.chars.afterCurrent() == '{') {
                if (buff.length > 0) {
                    this.parts.push(this.createTextPart(buff));
                }
                this.chars.next();
                this.readVar();
                buff = '';
                continue;
            }
            buff += currentChar;
        }

    }

    readVar() {
        var buff = '';
        while (this.chars.hasNext()) {
            var currentChar = this.chars.next();
            if (currentChar == '}' && this.chars.beforeCurrent() != '\\') {
                if (buff.length > 0) {
                    var varPart = this.tryCreateVarPart(buff);
                    if (varPart) {
                        this.parts.push(varPart);
                    } else {
                        this.parts.push(this.createTextPart(buff));
                    }
                }
                return;
            }
            buff += currentChar;
        }
    }

    createTextPart(text) {
        return {
            text: text,
            asString: function (data) {
                return this.text;
            }
        };
    }

    tryCreateVarPart(src) {
        var expression = new ExpressionParser().parse(src);
        if (expression) {
            return {
                expression: expression,
                asString: function (data) {
                    return this.expression.evaluate(data);
                }
            };
        }
        return false;
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


}

