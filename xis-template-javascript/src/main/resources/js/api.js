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




class ChildElementController {

    constructor(parent) {
        this.parent = parent;
        this.childNodes = toArray(parent.childNodes);
        this.repeats = {};
        this.showHide = {};
        this.repeatCaches = {};
    }


    unlinkElement(e) {
        e.parentNode.removeChild(e);
    }

    addRepeat(e) {
        var arr = doSplit(e.getAttribute('data-repeat'), ':');
        var arrayPath = doSplit(arr[0], '.');
        var varName = arr[1];
        this.unlinkElement(e);
        this.repeats[e] = { arrayPath: arrayPath, varName: varName };
        e.removeAttribute('data-repeat');
    }

    addShowHide(e) {
        this.unlinkElement(e);
        this.showHide[e] = new ExpressionParser().parse(e.getAttribute('data-show'));
    }

    refresh(data) {
        this.clear();
        this.childNodes.forEach(child => this.refreshElement(child, data));
    }

    refreshElement(e, data) {
        var visible = this.evaluateShowHide(e, data);
        if (this.repeats[e]) {
            if (visible) {
                this.evaluateRepeat(e, data);
            }
        } else {
            if (e.visible) {
                this.showElement(e);
            }
        }
    }

    clear() {
        this.childNodes.filter(e => e.visible).forEach(child => this.hideNode(child));
    }

    showVisibleElements() {
        this.childNodes.forEach(child => this.showIfVisible(child))
    }

    showIfVisible(child, data) {
        if (child.showHide) {
            if (!child.showHide.evaluate(data)) {
                return;
            }
        }
        if (child.repeat) {
            this.evaluateRepeat(child);
        } else {
            this.parent.appendChild(child);
        }
    }

    evaluateRepeat(child, data) {
        var cache = this.getRepeatCache();
        var arr = data.getValue(this.repeats[child].arrayPath);
        var valueKey = this.repeats[child].varName;
        var newData = new Data({});
        newData.parentData = data;
        for (var i = 0; i < arr.length; i++) {
            newData.setValue(valueKey, arr[i]);
            newData.setValue(valueKey + '_index', i);
            var element;
            if (cache.length <= i) {
                element = this.cloneNode(child);
                cache.push(element);
            } else {
                element = cache[i];
            }
            this.parent.appendChild(element);
            if (element.childController) {
                element.childController.refresh(newData);
            } else if (element.refresh) {
                element.refresh(element, newData);
            }
        }

    }

    evaluateShowHide(e, data) {
        var showHide = this.showHide[e];
        if (!showHide) return true;
        return showHide.evaluate(data);
    }

    hideNode(e) {
        if (e.visible) {
            this.parent.removeChild(e);
        }
        e.visible = false;
    }

    showElement(e) {
        this.parent.appendChild(e);
        e.visible = true;
    }

    cloneNode(node) {
        if (isElement(node)) {
            var newElement = document.createElement(node.localName);
            for (var attrName of node.getAttributeNames()) {
                newElement.setAttribute(attrName, node.getAttribute(attrName));
            }
            newElement.attrExpr = node.attrExpr;
            if (node.childController) {
                newElement.childController = node.childController.clone();
            }
            for (var i = 0; i < node.childNodes.length; i++) {
                newElement.appendChild(this.cloneNode(node.childNodes.item(i)));
            }
            newElement.refresh = node.refresh;
            newElement.attrExpr = node.attrExpr;
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

    getRepeatCache(e) {
        var cache = this.repeatCaches[e];
        if (!cache) {
            cache = [];
            this.repeatCaches[e] = cache;
        }
        return cache;
    }

}

class DocumentInitializer {

    constructor() {
        this.exprParser = new ExpressionParser();
    }

    initialize(node) {
        if (isElement(node)) {
            if (node.getAttribute('data-repeat')) {
                this.childController(node.parentNode).addRepeat(node);
            }
            if (node.getAttribute('data-show')) {
                this.childController(node.parentNode).addShowHide(node);
            }
            if (node.getAttribute('data-out')) {
                this.outExpr = this.exprParser.parse(node.getAttribute('data-out'));
            }
            node.attrExpr = {};
            for (var attrName of node.getAttributeNames()) {
                var attrValue = node.getAttribute(attrName);
                if (attrValue.indexOf('${') != -1) {
                    node.attrExpr[attrName] = new TextContentParser(attrValue).parse();
                }
            }
            node.refresh = (e, data) => {
                for (var attrName of Object.keys(e.attrExpr)) {
                    e.setAttribute(e.attrExpr[attrName].evaluate(data));
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
            for (var i = 0; i < node.childNodes.length; i++) {
                this.initialize(node.childNodes.item(i));
            }
        } else if (node.nodeValue && node.nodeValue.indexOf('${') != -1) {
            node.contentExpr = new TextContentParser(node.nodeValue).parse();
            node.refresh = (n, data) => n.nodeValue = n.contentExpr.evaluate(data);
        }
    }


    childController(element) {
        if (!element.childController) {
            element.childController = new ChildElementController(element);
        }
        return element.childController;
    }
}


class CharIterator {

    constructor(src) {
        this.src = src;
        this.index = -1;
    }

    hasNext() {
        return this.index + 1 < this.src.length;
    }

    current() {
        return this.src[this.index];
    }


    next() {
        this.index++;
        return this.src[this.index];
    }


    beforeCurrent() {
        return this.index - 1 > -1 ? this.src[this.index - 1] : undefined;
    }

    afterCurrent() {
        return this.index + 1 < this.src.length ? this.src[this.index + 1] : undefined;
    }

}



class TextContent {

    constructor(parts) {
        this.parts = parts;
    }


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
    constructor(values) {
        this.values = values;
        this.parentData = undefined;
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
    new DocumentInitializer().initialize(element);
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
        ]
    }));


}

