
class RootPageInitializer {

    /**
     * 
     * @param {NodeInitializer} nodeInitialier 
     */
    constructor(nodeInitialier) {
        this.nodeInitialier = nodeInitialier;
        this.head = getElementByTagName('head');
        this.body = getElementByTagName('body');
    }

    initialize() {
        this.head._bindChildNodes = function (headChildArray) {
            this._removeChildNodes();
            this._childNodes = headChildArray;
            for (var child of headChildArray) {
                if (child.localName !== title && child._refresh) {
                    child._refresh(data);
                }
            }
        }

        this.body._bindChildNodes = function (bodyChildArray) {
            this._removeChildNodes();
            this._childNodes = bodyChildArray;
            for (var child of bodyChildArray) {
                if (child.localName !== title && child._refresh) {
                    child._refresh(data);
                }
            }
        }

        this.body._bindBodyAttributes = function (attributes) {
            for (var name of Object.keys(attributes)) {
                this.setAttribute(name, attributes[name]);
            }
        }
    }
}



class NodeInitializer {

    /**
     * @public
     * @param {Node} node 
     */
    initializeNode(node) {
        if (isElement(node) && !node.getAttribute('data-ignore')) {
            this.initializeElement(node);
        } else {
            this.initializeTextNode(node);
        }
    }

    /**
     * @private
     * @param {Element} element 
     */
    initializeElement(element) {
        this.addElementStandardFields(element);
        this.addElementStandardMethods(element);

        if (element.getAttribute('data-repeat')) {
            this.initializeRepeat(element);
        }


        if (element.getAttribute('data-widget')) {
            this.initializeWidgetContainer(element);
        }

        this.initializeChildNodes(element);
    }

    /**
     * @private
     * @param {Element} element 
     */
    addElementStandardFields(element) {
        element._parent = element.parentNode;
        parent._childNodes.push(element);
    }

    /**
     * @private
     * @param {Element} element 
     */
    addElementStandardMethods(element) {

        element._refresh = function (data) {
            if (this._widgetIdExpression) {
                refreshWidgetContainer(element, data);
            }
            if (this._repeat) {
                refreshRepeat(this, data);
            }
        }

        element._refreshAttributes = function (data) {
            for (attribute of this._variableAttributes) {
                var value = attribute.expression.evaluate(data);
                element.setAttribute(attribute.name, value);
            }
        }

        element._refreshChildNodes = function (data) {
            var childArray = nodeListToArray(this.childNodes);
            for (let index = 0; index < childArray.length; index++) {
                var child = childArray[i];
                if (!child.getAttribute('data-ignore')) {
                    if (child.refresh) {
                        child._refresh(data);
                    }
                }
            }
        }

        element._removeChildNodes = function () {
            var childArray = nodeListToArray(this.childNodes);
            for (let index = 0; index < childArray.length; index++) {
                removeChildNode(childArray[i]);
            }
        }

    }
    /**
     * @private
     * @param {Element} element 
     */
    initializeRepeat(element) {
        var arr = doSplit(element.getAttribute('data-repeat'), ':');
        var valueKey = arr[0];
        var arrayExpression = this.exprParser.parse(arr[1]);
        element._repeat = {
            valueKey: valueKey,
            arrayExpression: arrayExpression,
            elements: []
        };
    }

    /**
     * @private
     * @param {Element} element 
     */
    initializeTextNode(node) {
        if (empty(node.nodeValue)) {
            node.paarentNode.removeChild(child);
        } else if (node.nodeValue && node.nodeValue.indexOf('${') != -1) {
            node._expression = new TextContentParser(node.nodeValue).parse();
            node._refresh = function (data) {
                this.nodeValue = this._expression.evaluate(data);
            }
        }
    }

    /**
     * @private
     * @param {Element} element 
     */
    initializeWidgetContainer(element) {
        element._widgetIdExpression = widgetIdExpression = new TextContentParser(element.getAttribute('data-widget')).parse();
    }

    initializeAttributes(element) {
        element._attributes = [];
        for (var attrName of element.getAttributeNames()) {
            var attrValue = element.getAttribute(attrName);
            if (attrValue.indexOf('${') != -1) {
                element._attributes.push({
                    name: attrName,
                    expression: new TextContentParser(attrValue).parse()
                });
            }
        }
    }


    /**
     * @private
     * @param {Element} element 
     */
    initializeChildNodes(element) {
        for (var i = 0; i < element.childNodes.length; i++) {
            this.initializeNode(element.childNodes[i]);
        }
    }
}


function refreshRepeat(origElement, loopAttributes, data) {
    var varName = loopAttributes.varName;
    var arrPath = loopAttributes.arrayExpression.evaluate(data);
    var dataArr = data.getValue(arrPath);
    if (!dataArr) {
        dataArr = [];
    }
    var elements = origElement._repeat.elements;
    var i = 0;
    var element = origElement;
    while (i < dataArr) {
        if (i >= elements.length) {
            var clone = cloneElement(element);
            appendSibling(element, clone);
            element = clone;
        } else {
            element = elements[i];
        }
        var value = dataArr[i];
        var data = new Data({}, parentData);
        data.setValue(varName, value);
        element.refresh(data);
        i++;
    }
    while (i < elements.length) {
        var element = elements[i];
        if (element.paarentNode) {
            element.paarentNode.removeChild(element);
        }
        i++;
    }
}

function refreshFor(element, loopAttributes, data) {
    var varName = loopAttributes.varName;
    var arrPath = loopAttributes.arrPath;
    var dataArr = data.getValue(arrPath);
    if (!dataArr) {
        dataArr = [];
    }
    var nodeArrays = element._forLoop.nodeArrays;
    var origNodeArray = nodeListToArray(element.childNodes);
    var i = 0;
    var e = element;
    while (i < dataArr) {
        var nodeArray;
        if (i >= nodeArrays.length) {
            var clones = cloneNodeArray(origNodeArray);
            for (var clone of clones) {
                appendSibling(e, clone);
            }
            nodeArray = clone;
        } else {
            nodeArray = nodeArrays[i];
        }
        e = lastArrayElement(nodeArray);
        var value = dataArr[i];
        var data = new Data({}, parentData);
        data.setValue(varName, value);
        for (var node of nodeArray) {
            if (node.refresh) {
                node.refresh(data);
            }
        }
        i++;
    }
    while (i < nodeArrays.length) {
        var nodeArray = nodeArrays[i];
        for (var node of nodeArray) {
            if (node.paarentNode) {
                node.paarentNode.removeChild(node);
            }
        }
        i++;
    }
}

function refresh(node, data) {
    if (node.refresh) {
        node.refresh(data);
    }
}

function refreshChildNodes(element, data) {
    for (let index = 0; index < element.childNodes.length; index++) {
        refresh(element.childNodes[index], data);
    }
}

function refreshChildNodeAsync(node) {
    var promises = [];
    for (let index = 0; index < element.childNodes.length; index++) {
        promises.add(new Promise((resolve, _) => {
            refresh(node, data);
            resolve();
        }));
    }
    Promise.all(promises);
}


function refreshWidgetContainer(element, parentData) {
    var widgetData = element._widgetData;
    var widgetId = element._widgetIdExpression.evaluate(parentData);
    client.loadWidgetData(widgetId, widgetData)
        .then(response => { element._widgetData = response.data; return element._widgetData; })
        .then(data => new Data(data, parentData))
        .then(data => { refreshAttributes(element, data); return data; })
        .then(data => refreshChildNodes(element, data));
}

/**
 * 
 * @param {Node} element 
 * @param {Node} sibling 
 */
function appendSibling(element, sibling) {
    var parent = element._parent;
    if (element.nextSibling) {
        parent.insertBefore(sibling, element.nextSibling);
    } else {
        parent.appendChild(sibling);
    }
}



function lastArrayElement(arr) {
    if (arr.length == 0) {
        return undefined;
    }
    return arr[arr.length - 1];
}


function isEmpyArray(arr) {
    return arr.length == 0;
}


class Cloner {

    /**
     * @public
     * @param {Node} node 
     * @returns {Node}
     */
    cloneNode(node) {
        if (isElement(node)) {
            return this.cloneElement(node);
        }
        return this.cloneTextNode(node);
    }

    /**
   * @private
   * @param {Node} element 
   * @returns 
   */
    cloneTextNode(node) {
        if (node._expression) {
            var cloned = document.createTextNode('');
            cloned._expression = node._expression;
            clone._refresh = function (data) {
                this.nodeValue = this._expression.evaluate(data);
            }
            return cloned;
        }
        return document.createTextNode(node.nodeValue);
    }


    /**
     * @private
     * @param {Element} element 
     * @returns 
     */
    cloneElement(element) {
        var newElement = document.createElement(element.localName);
        this.cloneAttributes(element, newElement);
        this.cloneFrameworkAttributes(element, newElement);
        this.cloneChildNodes(element, newElement);
        return newElement;
    }


    /**
     * @private
     * @param {Element} src 
     * @param {Element} dest 
     */
    cloneFrameworkAttributes(src, dest) {
        dest._variableAttributes = src._variableAttributes;
        dest._widgetIdExpression = src._widgetIdExpression;
        if (src._repeat) {
            dest._repeat = {
                varName: src._repeat.varName,
                arrayExpression: src._arrayExpression,
                elements: [] // do not clone
            };
        }
    }

    /**
     * @private
     * @param {Element} src 
     * @param {Element} dest 
     */
    cloneChildNodes(newElement, srcElement) {
        newElement._childNodes = [];
        for (let index = 0; index < srcElement.childNodes.length; index++) {
            var child = srcElement.childNodes.item(index);
            var clonedChild = this.clone(child);
            newElement.appendChild(clonedChild);
            newElement._childNodes.push(clonedChild);
            clonedChild._parent = newElement;
        }
    }

    /**
     * 
     * @param {Array<Node>} nodeArray 
     */
    cloneNodeArray(nodeArray) {
        return nodeArray.map(node => cloneNode(node));
    }
}

/** 
 * @param {Node} node 
 * @returns {boolean}
 */
function isElement(node) {
    return node instanceof HTMLElement;
}

/**
 * Maps a NodeList to an array.
 *
 * @public
 * @param {NodeList} nodeList
 * @returns {Array}
 */
function nodeListToArray(nodeList) {
    var arr = [];
    for (var i = 0; i < nodeList.length; i++) {
        arr.push(nodeList.item(i));
    }
    return arr;
}

function getElementByTagName(name) {
    return document.getElementsByTagName(name).item(0);
}