function defaultRefresh(element, data) {
    forChildElements(element, e => e.refresh(data))
}

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
        var e = element.cloneNode();
        e.removeAttribute('data-repeat');
        if (isElement(element)) {
            init(e);
        }
        return e;
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
        forChildElements(this.parent, child => child.refresh()); // refresh others

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
            e.refreshChildElements(data);
        }
        return true;
    }

    init() {
        var repeatAttribute = this.element.getAttribute(this.attributeName);
        var arr = doSplit(repeatAttribute, ':');
        this.arrayPath = doSplit(arr[0], '.');
        this.varName = arr[1];
    }
}

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

class Content {
    constructor(element) {
        this.element = element;
        this.attributeName = 'data-content';
        this.init();
    }

    init() {
        this.expression = new ScriptExpression('${' + this.element.getAttribute(this.attributeName) + '}');
    }

    refresh(data) {
        var content = this.expression.evaluate(data);
        this.element.innerText = content;
        return true;
    }
}


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




class Data {

    constructor(values) {
        this.values = values;
        this.parentData = undefined;
    }
    /**
     * 
     * @param {Element} element 
     * @returns 
     */
    getValue(path) {
        var dataNode = this.values;
        for (var i = 0; i < path.length; i++) {
            var key = path[i];
            if (dataNode[key]) {
                dataNode = dataNode[key];
            } else {
                break;
            }
        }
        if (dataNode === undefined && this.parentData) {
            return this.parentData.getValue(path)
        }
        return dataNode;
    }

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
function ifAttributePresent(element, attributeName, attributeCallback) {
    var attributeValue = element.getAttribute(attributeName);
    if (attributeValue) {
        attributeCallback(element, attributeValue);
    }
}


function forElements(nodeList, consumer) {
    for (var i = 0; i < nodeList.length; i++) {
        var node = nodeList.item(i);
        if (isElement(node)) {
            consumer(node);
        }
    }
}


function forChildElements(parent, consumer) {
    toArray(parent.childNodes).filter(n => isElement(n)).forEach(e => consumer(e));
}


function toArray(nodeList) {
    var arr = [];
    for (var i = 0; i < nodeList.length; i++) {
        arr.push(nodeList.item(i));
    }
    return arr;
}

function isElement(node) {
    return node instanceof HTMLElement;
}

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

function init(element) {
    element.repeats = [];
    ifAttributePresent(element, 'data-show', e => {
        e.selfRefresh = true;
        addRefreshShow(e);
    });
    ifAttributePresent(element, 'data-content', e => {
        addRefreshContent(element);
    });
    ifAttributePresent(element, 'data-repeat', e => {
        e.selfRefresh = true;
        addRepeat(element);
    });
    addRefesh(element);
    forChildElements(element, child => init(child));
}

function addRefreshShow(e) {
    e.show = new Show(element);
    e.refreshShow = data => e.show.refresh(data);
}

function addRefreshContent(e) {
    e.content = new Content(e);
    e.refreshContent = data => e.content.refresh(data);
}

function addRepeat(e) {
    e.parentNode.repeats.push(new Repeat(e));
}

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
        if (element.repeats.length == 0 && !element.refreshShow) {
            element.refreshChildElements(data);
        }
    }
    element.refreshChildElements = data => forChildElements(element, child => {
        if (!child.selfRefresh)
            child.refresh(data);
    });

}
function initPage() {
    var html = document.getElementsByTagName('html').item(0);
    init(html);
    html.refresh(new Data({
        title: 'bla', items: [{ name: 'name1' }, { name: 'name2' },]
    }));

}