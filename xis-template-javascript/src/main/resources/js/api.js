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

/**
 * Representation of the repeat-attribute.
 *
 * @public
 *
 */
class Repeat {

    /**
     * @param {Element} element
     */
    constructor(element) {
        this.element = element;
        this.element.explicitRefresh = true;
        this.parent = element.parentNode;
        this.nodeBehind = this.element.nextSibling;
        this.boundElements = [];
        this.parent.removeChild(element);
        this.arrayPath = [];
        this.varName = undefined;
        this.attributeName = attributeName(element, 'repeat')
        this.init();
    }

    /**
     * Creates a deep clone of an element.
     * The repeat attribute is removed, so the clones do not create
     * repeats.
     *
     * @private
     * @returns {Element}
     */
    clonedElement() {
        var clone = shallowCloneElement(this.element);
        clone.removeAttribute(this.attributeName);
        for (var i = 0; i < this.element.childNodes.length; i++) {
            var child = this.element.childNodes.item(i);
            clone.appendChild(cloneAndInitTree(child));
        }
        initElement(clone);
        clone.explicitRefresh = true;
        return clone;
    }

    /**
     * Refreshes the childnodes of this loop. To keep context, it
     * declares an own data-object for this repeat, where the given
     * data-object will be an ancestor.
     *
     * @public
     * @param {Data} parentData
     * @returns
     */
    refresh(parentData) {
        var data = new Data({});
        data.parentData = parentData;
        var dataArray = parentData.getValue(this.arrayPath);
        var indexName = this.varName + '_index';
        var index = 0;
        var e;
        var behind = this.nodeBehind;
        while (index < dataArray.length) {
            var dataValue = dataArray[index];
            data.setValue(this.varName, dataValue);
            data.setValue(indexName, 'index:' + index);
            if (this.boundElements.length <= index) {
                e = this.clonedElement();
                if (!behind) {
                    this.parent.appendChild(e);
                } else {
                    this.parent.insertBefore(e, behind);
                }
                this.boundElements.push(e);
            } else {
                e = this.boundElements[index];
            }
            index++
            e.refreshChildren(data);
            if (e.refreshShow && !e.refreshShow(data)) {
                continue;
            }
            if (e.refreshContent && !e.refreshContent(data)) {
                continue;
            }
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
class ShowAttribute {

    /**
     *
     * @param {Element} element
     */
    constructor(element) {
        this.element = element;
        this.parent = element.parentNode;
        this.nodeAfter = this.element.nextSibling;
        this.visible = true;
        this.attributeName = attributeName(element, 'show');
        this.init();
    }

    /**
     * @private
     */
    init() {
        var ifAttribute = this.element.getAttribute(this.attributeName);
        this.expression = new ExpressionParser().parse(ifAttribute);
    }

    /**
     * Invokes new decision, the children and deeper children will
     * be displayed.
     *
     * @param {*} data
     * @returns
     */
    refresh(data) {
        var state = this.expression.evaluate(data);
        if (state) this.doShow();
        else this.doHide();
        return state;
    }

    /**
     * @private
     * Causes this element to be visible, in case it's currently hidden.
     */
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

    /**
     * @private
     * Hides this element, in case it is curently visible.
     */
    doHide() {
        this.parent.removeChild(this.element);
        this.visible = false;
    }
}

/**
 * Representation of an attribute for displaying data.
 */
class OutAttribute {

    constructor(element) {
        this.element = element;
        this.attributeName = attributeName(element, 'out');
    }

    /**
     * @public
     */
    init() {
        this.expression = new ExpressionParser().parse(this.element.getAttribute(this.attributeName));
    }

    /**
     * Re-builds the content of this tag.
     * @public
     * @param {Data} data 
     * @returns 
     */
    refresh(data) {
        var content = this.expression.evaluate(data);
        this.element.innerText = content;
        return true;
    }


    cloneAttribute(e) {

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
 * The attribute for the given short-name in use.
 * 
 * @param {Element} element 
 * @param {string} name 
 * @returns the short name or the name prefixed with 'data-'
 */
function attributeName(element, name) {
    var attr = element.getAttribute(name);
    if (attr) {
        return name;
    }
    name = 'data-' + name;
    return element.getAttribute(name) ? name : undefined;
}


/**
 * 
 * @param {Element} element 
 * @param {String} attributeName 
 * @param {attributeCallback} 
 */
function forAttributeDo(element, attributeName, attributeCallback) {
    var attributeValue = element.getAttribute(attributeName) || element.getAttribute('data-' + attributeName);
    if (attributeValue) {
        attributeCallback(element);
    }
}


/**
 * @public
 * @param {Element} parent 
 * @param {elementConsumer} will be executed for each element
 */
function forChildElements(parent, consumer) {
    toArray(parent.childNodes).filter(n => isElement(n)).forEach(e => consumer(e));
}

/**
 * 
 * @public
 * @param {Element} parent
 * @param {nodeConsumer} will be executed for each node
 */
function forChildNodes(parent, consumer) {
    toArray(parent.childNodes).forEach(e => consumer(e));
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
/**
 * 
 * Initialzes the given tree or subtree, which means
 * adding some methods to the dom element.
 *
 * @public
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

/**
 * Creates a deep clone of the given node.
 *
 * @param {Node} node
 * @returns
 */
function cloneTree(node) {
    if (isElement(node)) {
        var clone = shallowCloneElement(node);
        for (var i = 0; i < node.childNodes.length; i++) {
            var child = node.childNodes.item(i);
            clone.appendChild(cloneTree(child));
        }
        return clone;
    } else {
        return cloneTextNode(node);
    }
}

/**
 * Creates a deep clone of the given node and runs initialization,
 * which means adding some methods to the dom element.
 *
 * @param {Node} node
 * @returns
 */
function cloneAndInitTree(node) {
    if (isElement(node)) {
        var clone = initElement(shallowCloneElement(node));
        for (var i = 0; i < node.childNodes.length; i++) {
            var child = node.childNodes.item(i);
            clone.appendChild(cloneAndInitTree(child));
        }
        return clone;
    } else {
        return initTextNode(cloneTextNode(node));
    }
}

/**
 * Runs initialization for an element, which means adding some methods
 * to the dom element.
 *
 * @param {Element} element
 * @returns
 */

function initElement(element) {
    element.repeats = [];
    forAttributeDo(element, 'show', addRefreshShow);
    forAttributeDo(element, 'out', addRefreshOut);
    forAttributeDo(element, 'repeat', addRepeat);
    addRefesh(element);
    return element;
}

function shallowCloneElement(element) {
    var newElement = document.createElement(element.localName);
    for (var attrName of element.getAttributeNames()) {
        newElement.setAttribute(attrName, element.getAttribute(attrName));
    }
    return newElement;
}

function cloneTextNode(node) {
    var clonedNode = document.createTextNode(node.textContent);
    if (node.expression) {
        clonedNode.expression = node.expression;
        clonedNode.refresh = data => clonedNode.nodeValue = clonedNode.expression.evaluate(data);
    }
    return clonedNode;
}


function initTextNode(node) {
    if (node.nodeValue.indexOf('${') != -1) {
        node.content = new TextContentParser(node.nodeValue).parse();
        node.refresh = data => node.nodeValue = node.content.evaluate(data);
    }
    return node;
}


/**
 * @param {Element} e 
 */
function addRefreshShow(e) {
    e.show = new ShowAttribute(element);
    e.refreshShow = data => e.show.refresh(data);
}

/**
 * @param {Element} e 
 */
function addRefreshOut(e) {
    e.out = new OutAttribute(e);
    e.out.init();
    e.refreshContent = data => e.out.refresh(data);
}


function cloneRefreshOut(src, target) {
    target.con
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
        if (!child.explicitRefresh && child.refresh) {
            child.refresh(data);
        }
    });

}



/**
 * 
 */
function initPage() {
    var html = document.getElementsByTagName('html').item(0);
    initTree(html);
    html.refresh(new Data({
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

