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

class ContainerController {
    constructor(containerId) {
        this.containerId = containerId;
    }

    refresh(data, httpParameters) {

    }

    submitForm(form) {

    }

    linkInvocated() {

    }
}



class ChildController {

    constructor(parent) {
        this.parent = parent;
        this.childNodes = toArray(parent.childNodes);
        this.repeats = new Array(parent.childNodes.length);
        this.showHide = new Array(parent.childNodes.length);
        this.repeatCaches = {};
        this.expressionParser = new ExpressionParser();
    }


    unlinkElement(e) {
        e.parentNode.removeChild(e);
    }

    addRepeat(e, index) {
        var arr = doSplit(e.getAttribute('data-repeat'), ':');
        var expression = this.expressionParser.parse(arr[1]);
        var varName = arr[0];
        this.unlinkElement(e);
        this.repeats[index] = { expression: expression, varName: varName };
        e.removeAttribute('data-repeat');
    }

    addShowHide(e, index) {
        this.unlinkElement(e);
        this.showHide[index] = new ExpressionParser().parse(e.getAttribute('data-show'));
    }

    refresh(data) {
        this.clear();
        for (var i = 0; i < this.childNodes.length; i++) {
            var child = this.childNodes[i];
            this.refreshNode(child, data, i);
        }
    }

    refreshNode(node, data, index) {
        if (this.evaluateShowHide(node, data, index)) {
            if (this.repeats[index]) {
                this.evaluateRepeat(node, data, this.repeats[index]);
            } else {
                this.parent.doAppendChild(this.parent, node);
                if (node.refresh) {
                    node.refresh(node, data);
                }
            }
        }
    }

    clear() {
        this.childNodes.forEach(child => this.parent.doRemoveChild(this.parent, child));
    }

    evaluateRepeat(child, data, repeat) {
        var cache = this.getRepeatCache();
        var arr = repeat.expression.evaluate(data);
        if (!arr) return;
        var valueKey = repeat.varName;
        var newData = new Data({}, data);
        for (var i = 0; i < arr.length; i++) {
            newData.setValue(valueKey, arr[i]);
            newData.setValue(valueKey + '_index', i);
            var element;
            if (cache.length <= i) {
                cache.push(this.cloneNode(child));
            }
            element = cache[i];
            element.childIndex = i;// we need this for selectedIndex
            this.parent.appendChild(element);
            if (element.childController) {
                element.childController.refresh(newData);
            } else if (element.refresh) {
                element.refresh(element, newData);
            }
        }

    }

    evaluateShowHide(e, data, index) {
        var showHide = this.showHide[index];
        if (!showHide) return true;
        return showHide.evaluate(data);
    }

    cloneNode(node) {
        if (isElement(node)) {
            var newElement = document.createElement(node.localName);
            for (var attrName of node.getAttributeNames()) {
                newElement.setAttribute(attrName, node.getAttribute(attrName));
            }
            newElement.attrExpr = node.attrExpr;
            if (node.childController) {
                newElement.childController = node.childController.cloneForElement(newElement);
            } else {
                for (var i = 0; i < node.childNodes.length; i++) {
                    newElement.appendChild(this.cloneNode(node.childNodes.item(i)));
                }
            }
            newElement.refresh = node.refresh;
            newElement.attrExpr = node.attrExpr;
            newElement.selectedExpr = node.selectedExpr;
            newElement.doAppendChild = node.doAppendChild;
            newElement.doRemoveChild = node.doRemoveChild;
            return newElement;
        } else {
            var newNode = document.createTextNode(node.nodeValue);
            if (node.contentExpr) {
                newNode.contentExpr = node.contentExpr;
                newNode.refresh = node.refresh;
            }
            return newNode;
        }
    }

    cloneForElement(element) {
        var controller = new ChildController(element);
        controller.repeats = this.repeats;
        controller.showHide = this.showHide;
        for (var child of this.childNodes) {
            controller.childNodes.push(this.cloneNode(child));
        }
        return controller;
    }

    getRepeatCache(e) {
        var cache = this.repeatCaches[e];
        if (!cache) {
            cache = [];
            this.repeatCaches[e] = cache;
        }
        return cache;
    }

}

/**
 * Decorates elements of the document 
 * with faremwork classes and methods.
 */
class DocumentInitializer {

    constructor() {
        this.exprParser = new ExpressionParser();
    }

    /**
     * Decorates the document of the given root
     * @public
     * @param {Element} root 
     */
    initializeDocument(root) {
        this.decorateElement(root);
        this.initialize(root);
    }

    /**
     * @private
     * @param {Element} parent 
     */
    initialize(parent) {
        for (var i = 0; i < parent.childNodes.length; i++) {
            var child = parent.childNodes.item(i);
            if (isElement(child)) {
                this.decorateElement(child);
                this.decorateChildElement(child, i);
                this.initialize(child);
            } else if (child.nodeValue && child.nodeValue.indexOf('${') != -1) {
                child.contentExpr = new TextContentParser(child.nodeValue).parse();
                child.refresh = (n, data) => n.nodeValue = n.contentExpr.evaluate(data);
            }
        }
    }

    /**
    * @private
    * @param {Element} parent 
    */
    decorateElement(element) {
        element.refresh = (e, data) => {
            for (var attrName of Object.keys(e.attrExpr)) {
                e.setAttribute(attrName, e.attrExpr[attrName].evaluate(data));
            }
            if (e.selectedExpr) {
                if (e.getAttribute('value') == e.selectedExpr.evaluate(data)) {
                    e.setAttribute("selected", true); // TODO test
                } else {
                    e.removeAttribute('selected');
                }
            }
            if (e.childController) {
                e.childController.refresh(data);
            } else {
                for (var i = 0; i < e.childNodes.length; i++) {
                    var childNode = e.childNodes.item(i);
                    if (childNode.refresh) {
                        childNode.refresh(childNode, data);
                    }
                }
            }
        };
        element.doAppendChild = (e, child) => {
            if (!e.childArray) {
                e.childArray = [];
            }
            e.childArray.push(e);
            e.appendChild(child);
        };

        element.doRemoveChild = (e, child) => {
            if (e.childArray) {
                var index = e.childArray.indexOf(child);
                if (index != -1) {
                    e.childArray.splice(index, 1);
                    e.removeChild(child);
                }
            }
        };

        element.attrExpr = {};
        for (var attrName of element.getAttributeNames()) {
            var attrValue = element.getAttribute(attrName);
            if (attrValue.indexOf('${') != -1) {
                element.attrExpr[attrName] = new TextContentParser(attrValue).parse();
            }
        }
    }

    decorateChildElement(element, childIndex) {
        if (element.getAttribute('data-repeat')) {
            this.childController(element.parentNode).addRepeat(element, childIndex);
        }
        if (element.getAttribute('data-show')) {
            this.childController(element.parentNode).addShowHide(element, childIndex);
        }
        if (element.getAttribute('data-selected')) {
            element.selectedExpr = this.exprParser.parse(element.getAttribute('data-selected'));
        }
    }

    childController(element) {
        if (!element.childController) {
            element.childController = new ChildController(element);
        }
        return element.childController;
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

function initTree(element) {
    new DocumentInitializer().initializeDocument(element);
}

/**
 * 
 */
function initPage() {
    var html = document.getElementsByTagName('html').item(0);
    initTree(html);
    html.refresh(html, new Data({
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

