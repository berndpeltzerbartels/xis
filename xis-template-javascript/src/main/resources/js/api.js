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
        var indexName = this.varName + '-index';
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
class Show {

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
        this.expression = new ScriptExpressionParser(ifAttribute).tryParse();
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
class Out {
    constructor(element) {
        this.element = element;
        this.attributeName = attributeName(element, 'out');
        this.init();
    }

    /**
     * @private
     */
    init() {
        this.expression = new ScriptExpressionParser(this.element.getAttribute(this.attributeName)).tryParse();
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
}

/**
 * Represents a textnode
 */
class ContentNode {

    /**
     * 
     * @param {Text} textNode 
     */
    constructor(textNode) {
        this.node = textNode;
        this.textContent = new TextWithVarsParser(node.nodeValue).parse();
    }

    /**
     * @public
     * @param {Data} data
     */
    refresh(data) {
        this.node.nodeValue = this.textContent.evaluate(data);
    }
}

class StringParameterToken {

    constructor(string, length) {
        this.string = string;
        this.lenght = length;
    }

    toScript() {
        var script = '\'';
        script += this.string;
        script += '\'';
        return script;
    }
}

class StringParameterTokenParser {

    constructor(src) {
        this.src = src;
        console.log('src:' + this.src);
        this.start = 0;
        this.end = 0;
        this.string = '';
        this.buff = '';
    }

    tryParse() {
        if (this.parse()) {
            var length = this.end + 1;
            return new StringParameterToken(this.string, length);
        }
        return false;
    }

    length() {
        return this.length;
    }

    parse() {
        console.log('src:' + this.src);
        if (this.src.length == 0) {
            return false;
        }
        if (this.src.charAt(0) !== '\'') {
            return false;
        }
        this.end++;
        while (this.end < this.src.length) {
            var ch = this.src.charAt(this.end);
            console.log('ch=' + ch);
            var nextChar = this.end + 1 < this.src.length ? this.src.charAt(this.end + 1) : undefined;
            if (ch == '\\' && nextChar == '\'') {
                this.end++;
                continue;
            }
            if (ch == '\'') {
                break;
            }
            this.string += ch;
            this.end++;

        }
        return this.string.length > 0;

    }

}


class NumberParameterToken {

    constructor(number, length) {
        this.number = number;
        this.length = length;
    }

    toScript() {
        return '' + this.number;
    }
}

class NumberParameterTokenParser {

    constructor(src) {
        this.src = src;
        this.start = 0;
        this.end = 0;
        this.dots = 0;
        this.negative = false;
        this.buff = '';
    }

    tryParse() {
        if (this.parse()) {
            var number = this.dots > 0 ? parseFloat(this.buff) : parseInt(this.buff);
            if (this.negative) {
                number *= -1;
            }
            var length = this.end + 1;
            return new NumberParameterToken(number, length);
        }
        return false;
    }

    length() {
        return this.length;
    }

    parse() {
        if (this.src.length == 0) {
            return false;
        }
        if (this.src.charAt(0) == '-') {
            this.negative = true;
            this.end++;
        }
        for (; this.end < this.src.length; this.end++) {
            var ch = this.src.charAt(this.end);
            if (ch >= 0 && ch <= 9) {
                this.buff += ch;
                continue;
            }
            if (ch == '.') {
                if (this.dots > 0) {
                    return false;
                }
                this.dots++;
                this.buff += ch;
                continue;
            }
            break;
        }
        this.end--;
        return this.buff.length > 0;
    }


}


class VarParameterToken {

    constructor(varPath, length) {
        this.varPath = varPath;
        this.length = length;
    }

    toScript() {
        var script = 'data.getValue(';
        script += arrayAsString(this.varPath);
        script += ')';
        return script;
    }

}

class VarParameterTokenParser {

    constructor(src) {
        this.src = src;
        this.start = 0;
        this.end = 0;
        this.varPath = [];
    }

    tryParse() {
        if (this.parse()) {
            var length = this.end + 1;
            return new VarParameterToken(this.varPath, length);
        }
        return false;
    }

    length() {
        return this.length;
    }

    parse() {
        if (this.src.length == 0) {
            return false;
        }
        var ch = this.src.charAt(this.end);
        if (ch >= '0' && ch <= '9') {
            return false;
        }
        var buff = '';
        for (; this.end < this.src.length; this.end++) {
            var ch = this.src.charAt(this.end);
            if (isWhitespace(ch) || ch == ',') {
                this.end--;
                break;
            }
            if (ch == '.') {
                if (buff.length > 0) {
                    this.varPath.push(buff);
                    buff = '';
                    continue;
                }
                return false;
            }
            if (ch == ')') {
                this.end--;
                break;
            }
            if (ch == '(') {
                this.end--;
                return false;
            }
            buff += ch;
        }
        if (buff.length > 0) {
            this.varPath.push(buff);
        }
        return this.varPath.length > 0;
    }


}


class ParameterListToken {

    constructor(parameterTokens, length) {
        this.parameterTokens = parameterTokens;
        this.length = length;
    }

    toScript() {
        return this.parameterTokens.map(token => token.toScript()).join(',');
    }

}


class ParameterListTokenParser {

    constructor(src) {
        this.src = src;
        this.start = 0;
        this.end = this.src.length - 1;
        this.type = undefined;
        this.parameterTokens = [];
    }

    tryParse() {
        if (this.parse()) {
            var length = this.src.length;
            return new ParameterListToken(this.parameterTokens, length);
        }
        return false;
    }

    length() {
        return this.length;
    }

    parse() {
        var ch = this.src.charAt(this.start);
        if (ch != '(') {
            return false;
        }
        this.start++;
        while (this.start < this.end) {
            var ch = this.src.charAt(this.start);
            switch (ch) {
                case ')': return true;
                case ',': {
                    this.start++;
                    continue;
                }
                default: {
                    this.skipWiteSpaces();
                    var token = this.parseParameter();
                    if (!token) {
                        return false;
                    }
                    this.parameterTokens.push(token);
                    this.start += token.length
                    this.skipWiteSpaces();
                }
            }
            if (this.src.charAt(this.start) == ',') {
                continue;
            }
        }
        return true;
    }


    skipWiteSpaces() {
        while (this.start < this.end) {
            var ch = this.src.charAt(this.start);
            if (isWhitespace(ch)) {
                this.start++;
                continue;
            }
            break;

        }
    }


    parseParameter() {
        var src = this.src.substring(this.start);
        var parser = new StringParameterTokenParser(src);
        var token = parser.tryParse();
        if (token) {
            return token;
        }
        parser = new NumberParameterTokenParser(src);
        token = parser.tryParse();
        if (token) {
            return token;
        }
        parser = new VarParameterTokenParser(src);
        token = parser.tryParse();
        if (token) {
            return token;
        }
        return new FunctionTokenParser(src).tryParse();
    }
}

class FunctionToken {

    constructor(functionName, parameterListToken) {
        this.functionName = functionName;
        this.parameterListToken = parameterListToken;
        this.length = functionName.length + this.parameterListToken.length;
    }

    toScript() {
        var script = this.functionName;
        script += '(';
        script += this.parameterListToken.toScript();
        script += ')';
        return script;
    }
}

class FunctionTokenParser {

    constructor(src) {
        this.src = src;
        this.start = 0;
        this.end = this.src.length - 1;
        this.functionName = '';
    }

    tryParse() {
        if (this.parse()) {
            var substr = this.src.substring(this.start, this.end + 1);
            var parameterListTokenParser = new ParameterListTokenParser(substr);
            var parameterListToken = parameterListTokenParser.tryParse();
            if (!parameterListToken || this.functionName.length == 0) {
                return false;
            }
            return new FunctionToken(this.functionName, parameterListToken);
        }
        return false;
    }

    length() {
        return this.length;
    }

    parse() {
        var endChar = this.src.charAt(this.end);
        console.log(endChar);
        if (endChar !== ')') {
            return false;
        }
        while (this.start < this.end) {
            var ch = this.src.charAt(this.start++);
            console.log(ch);
            if (ch == '$' || ch == '{' || ch == '}') {
                return false;
            } else if (ch == '(') {
                this.start--;
                break;
            } else {
                this.functionName += ch;
            }
        }
        return true;
    }


}


function isWhitespace(ch) {
    return ch == ' ' || ch == "\t" || ch == "\n" || ch == "\r";
}


class ScriptExpression {

    constructor(script) {
        this.script = script;
    }

    /**
    * Evaluates the script for the given data.
    *
    * @param {Data} data
    * @returns
    */
    evaluate(data) {
        return eval(this.script);
    }

}

class ScriptExpressionParser {

    constructor(src) {
        this.src = src;
        this.start = 0;
        this.end = this.src.length - 1;
    }


    /**
     * Creates a script object by parsing a textnode.
     * @private
     */
    tryParse() {
        var rootToken = this.rootToken();
        if (rootToken) {
            var script = rootToken.toScript();
            return new ScriptExpression(script);
        }
        return false;
    }


    rootToken() {
        var parser = new VarParameterTokenParser(this.src);
        var token = parser.tryParse();
        if (token) {
            return token;
        }
        return new FunctionTokenParser(this.src).tryParse();
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



class TextWithVars {

    constructor(parts) {
        this.parts = parts;
    }


    evaluate(data) {
        return this.parts.map(part => part.asString(data)).reduce((s1, s2) => s1 + s2);
    }

}

class TextWithVarsParser {

    constructor(src) {
        this.chars = new CharIterator(src);
        this.parts = [];
    }

    parse() {
        this.readText();
        return new TextWithVars(this.parts);
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
        var expression = new ScriptExpressionParser(src).tryParse();
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

function arrayAsString(arr) {
    return "['" + arr.join("','") + "']";
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
function forAttribute(element, attributeName, attributeCallback) {
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
    forAttribute(element, 'show', addRefreshOut);
    forAttribute(element, 'out', addRefreshOut);
    forAttribute(element, 'repeat', addRepeat);
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
        var textWithVars = new TextWithVarsParser(node.nodeValue).parse();
        node.textWithVars = textWithVars;
        node.refresh = data => node.nodeValue = node.textWithVars.evaluate(data);
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
        if (!child.explicitRefresh && child.refresh) {
            child.refresh(data);
        }
    });

    function identity(o) {
        return o;
    }

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


//console.log(new TextContent('Heute ist ${format(date)}').parse());