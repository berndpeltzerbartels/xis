
class Repeat {

    constructor(element) {
        this.element = element;
        this.parent = element.parentNode;
        this.nodeAfter = this.element.nextSibling;
        this.elementCache = [element];
        this.arrayPath = [];
        this.varName = undefined;
        this.attributeName = 'data-repeat';
        this.init();
    }

    childIndex(child) {
        for (var i = 0; i < this.parent.childNodes.length; i++) {
            if (this.parent.childNodes.item(i) === child) {
                return i;
            }
        }
    }

    resizeCache(size) {
        while (this.elementCache.length < size) {
            var e = this.cloneNode(this.element);
            this.elementCache.push(e);
        }
    }

    cloneNode(element) {
        var clone = cloneElement(element);
        clone.removeAttribute(this.attributeName);
        for (var i = 0; i < element.childNodes.length; i++) {
            var child = element.childNodes.item(i);
            clone.appendChild(cloneAndInitTree(child));
        }
        initElement(clone);
        return clone;
    }


    clearChildren() {
        for (var i = 0; i < this.elementCache.length; i++) {
            this.parent.removeChild(this.elementCache[i]);
        }
    }

    refresh(parentData) {
        var data = new Data({});
        data.parentData = parentData;
        var dataArray = parentData.getValue(this.arrayPath);
        this.clearChildren();
        this.resizeCache(dataArray.length);
        var behind;
        var i = 0;
        for (var i = dataArray.length - 1; i > -1; i--) {
            var dataValue = dataArray[i];
            data.setValue(this.varName, dataValue);
            var e = this.elementCache[i];
            if (!behind) {
                behind = this.nodeAfter;
            }
            if (behind) {
                this.parent.insertBefore(e, behind);
            } else {
                this.parent.appendChild(e);
            }
            behind = e;
            if (e.refreshShow && !e.refreshShow(data)) {
                continue;
            }
            if (e.refreshContent && !e.refreshContent(data)) {
                continue;
            }
            e.refreshChildren(data);
        }
        return true;
    }

    /**
     * @private
     */
    init() {
        var repeatAttribute = this.element.getAttribute(this.attributeName);
        var arr = doSplit(repeatAttribute, ':');
        this.arrayPath = doSplit(arr[0], '.');
        this.varName = arr[1];
    }
}

/**
 * Representation of an attribute containing an expression deciding 
 * show or hide the tag and its content.
 * 
 */
class Show {

    constructor(element) {
        this.element = element;
        this.parent = element.parentNode;
        this.nodeAfter = this.element.nextSibling;
        this.visible = true;
        this.attributeName = 'data-show';
        this.init();
    }

    init() {
        var ifAttribute = this.element.getAttribute(this.attributeName);
        this.expression = new ScriptExpression(ifAttribute);
    }

    refresh(data) {
        var state = this.expression.evaluate(data);
        if (state) this.doShow();
        else this.doHide();
        return state;
    }

    doShow() {
        if (!this.visible) {
            if (this.nodeAfter) {
                this.parent.insertBefore(this.element, this.nodeAfter);
            } else {
                this.parent.appendChild(this.element);
            }
            this.visible = true;
        }
        forChildElements(this.parent, child => child.refresh());

    }
    doHide() {
        this.parent.removeChild(this.element);
        this.visible = false;
    }
}

/**
 * Representation of an attribute for displaying data.
 */
class Out {
    constructor(element) {
        this.element = element;
        this.attributeName = 'data-out';
        this.init();
    }

    /**
     * @private
     */
    init() {
        this.expression = new ScriptExpression('${' + this.element.getAttribute(this.attributeName) + '}');
    }

    /**
     * @public
     * @param {Data} data 
     * @returns 
     */
    refresh(data) {
        var content = this.expression.evaluate(data);
        this.element.innerText = content;
        return true;
    }
}

class ContentNode {

    /**
     * 
     * @param {Text} textNode 
     */
    constructor(textNode) {
        this.node = textNode;
        this.expression = new ScriptExpression(node.nodeValue);
    }

    refresh(data) {
        this.node.nodeValue = this.expression.evaluate(data);
    }
}

/**
 * Javascriptcode container $-Variables (${...})
 */
class ScriptExpression {

    constructor(src) {
        this.src = src;
        this.pos = 0;
        this.script = '';
        this.parseScript();
    }

    evaluate(data) {
        return eval(this.script);
    }

    parseScript() {
        this.script = this.tokens().map(token => this.tokenAsString(token)).join('+');
    }

    tokenAsString(token) {
        return token.type == 'var' ? this.varTokenAsString(token) : this.stringTokenAsString(token);
    }

    stringTokenAsString(token) {
        return token.content;
    }

    varTokenAsString(token) {
        return 'data.getValue(' + this.arrayAsString(doSplit(token.content, '.')) + ')';
    }

    tokens() {
        var rv = [];
        var readVar = false;
        var buff = '';
        for (var i = 0; i < this.src.length; i++) {
            var ch = this.src.charAt(i);
            if (ch == '\\') {
                // escaped
                buff += ch;
                i++;
                if (this.src.length > i) {
                    buff += this.src.charAt(i);
                    continue;
                } else {
                    break;
                }
            }
            if (ch == '}' && readVar) {
                if (buff.length > 0) {
                    rv.push({ type: 'var', content: buff });
                    buff = '';
                    readVar = false;
                    continue;
                }
            }
            if (ch == '$' && !readVar && i + 1 < this.src.length && this.src.charAt(i + 1) == '{') {
                if (buff.length > 0)
                    rv.push({ type: 'string', content: buff });
                buff = '';
                readVar = true;
                i++;
                continue;
            }
            buff += ch;
        }
        if (buff.length > 0) {
            rv.push({ type: 'string', content: buff });
        }
        return rv;
    }

    arrayAsString(arr) {
        return "['" + arr.join("','") + "']";
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
     * @param {Array}
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
 * 
 * @param {Element} element 
 * @param {String} attributeName 
 * @param {attributeCallback} 
 */
function forAttribute(element, attributeName, attributeCallback) {
    var attributeValue = element.getAttribute(attributeName);
    if (attributeValue) {
        attributeCallback(attributeValue);
    }
}


/**
 * 
 * @param {Element} parent 
 * @param {*} consumer TODO
 */
function forChildElements(parent, consumer) {
    toArray(parent.childNodes).filter(n => isElement(n)).forEach(e => consumer(e));
}

/**
 * 
 * @param {Element} parent 
 * @param {*} consumer TODO
 */
function forChildNodes(parent, consumer) {
    toArray(parent.childNodes).forEach(e => consumer(e));
}



/**
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

function cloneNode(element) {
    var e = cloneAndInitElement(element);
    e.removeAttribute('data-repeat');
    e.selfRefresh = true;
    return e;
}

/**
 * 
 * @param {Element} node 
 */
function initTree(node) {
    if (isElement(node)) {
        initElement(node);
    } else {
        initTextNode(node);
    }
    forChildNodes(node, child => initTree(child));
}

function cloneTree(node) {
    if (isElement(node)) {
        var clone = cloneElement(node);
        for (var i = 0; i < node.childNodes.length; i++) {
            var child = node.childNodes.item(i);
            clone.appendChild(cloneTree(child));
        }
        return clone;
    } else {
        return cloneTextNode(node);
    }
}

function cloneAndInitTree(node) {
    if (isElement(node)) {
        var clone = initElement(cloneElement(node));
        for (var i = 0; i < node.childNodes.length; i++) {
            var child = node.childNodes.item(i);
            clone.appendChild(cloneAndInitTree(child));
        }
        return clone;
    } else {
        return initTextNode(cloneTextNode(node));
    }
}

function initElement(element) {
    element.repeats = [];
    forAttribute(element, 'data-show', _ => {
        addRefreshShow(element);
    });
    forAttribute(element, 'data-out', _ => {
        addRefreshOut(element);
    });
    forAttribute(element, 'data-repeat', _ => {
        addRepeat(element);
    });
    addRefesh(element);
    return element;
}

function cloneElement(element) {
    var newElement = document.createElement(element.localName);
    for (var attrName of element.getAttributeNames()) {
        newElement.setAttribute(attrName, element.getAttribute(attrName));
    }
    return newElement;
}

function cloneTextNode(node) {
    return document.createTextNode(node.innerText);
}


function initTextNode(node) {
    if (node.nodeValue.indexOf('${') != -1) {
        node.expression = new ScriptExpression(node.nodeValue);
        node.refresh = data => node.nodeValue = node.expression.evaluate(data);
    }
    return node;
}


/**
 * @param {Element} e 
 */
function addRefreshShow(e) {
    e.show = new Show(element);
    e.refreshShow = data => e.show.refresh(data);
}

/**
 * @param {Element} e 
 */
function addRefreshOut(e) {
    e.content = new Out(e);
    e.refreshContent = data => e.content.refresh(data);
}

/**
 * @param {Element} e 
 */
function addRepeat(e) {
    e.parentNode.repeats.push(new Repeat(e));
}

/**
 * @param {Element} e 
 */
function addRefesh(element) {
    element.refresh = data => {
        if (element.refreshShow && !element.refreshShow(data)) {
            return;
        }
        for (var repeat of element.repeats) {
            repeat.refresh(data);
        }
        if (element.refreshContent) {
            element.refreshContent(data);
        }
        element.refreshChildren(data);
    }
    element.refreshChildren = data => forChildNodes(element, child => {
        if (!child.selfRefresh && child.refresh)
            child.refresh(data);
    });

}
/**
 * 
 */
function initPage() {
    var html = document.getElementsByTagName('html').item(0);
    initTree(html);
    html.refresh(new Data({
        title: 'bla', items: [{ name: 'name1' }, { name: 'name2' },]
    }));

}