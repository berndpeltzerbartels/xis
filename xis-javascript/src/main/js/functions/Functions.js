/**
 * @param {Node} node
 * @returns {boolean}
 */
function isElement(node) {
    return node.nodeType == 1;
}

/**
 * Finds a unique element
 * 
 * @param {string} name 
 * @returns 
 */
function getElementByTagName(name) {
    var list = document.getElementsByTagName(name);
    switch (list.length) {
        case 0: return null;
        case 1: return list.item(0);
        default: throw new Error('too many results for ' + name);
    }
}

/**
 * @param {Element} parent 
 * @param {string} childName 
 * @returns {Element}
 */
function getChildElementByName(parent, childName) {
    for (var i = 0; i < parent.childNodes.length; i++) {
        var child = parent.childNodes.item(i);
        if (isElement(child) && child.localName == childName) {
            return child;
        }
    }
}

/**
 * 
 * @param {Element} parent 
 * @returns {Element}
 */
function getFirstChildElement(parent) {
    for (var i = 0; i < parent.childNodes.length; i++) {
        var child = parent.childNodes.item(i);
        if (isElement(child)) {
            return child;
        }
    }
}
/**
 * Maps a NodeList into an array.
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


/**
 *
 * @param {string} str
 * @returns
 */
function trim(str) {
    var start = 0;
    for (; start < str.length; start++) {
        if (!isWhitespace(str.charAt(start))) {
            break;
        }
    }
    var end = str.length - 1;
    for (; end >= start; end--) {
        if (!isWhitespace(str.charAt(end))) {
            break;
        }
    }
    return str.substring(start, end + 1);

}


function isWhitespace(c) {
    return c == ' ' || c == '\t' || c == '\n' || c == '\r';
}

function cloneArr(arr) {
    return arr.map(v => v);
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

/**
 * For better mocking
 * @param {string} name 
 */
function createElement(name) {
    return document.createElement(name);
}


function initializeElement(element) {
    app.initializer.initialize(element);
}

function assertNotNull(o, errorText) {
    if (!errorText) errorText = 'Assertion failed. Value is null.'
    if (!o) throw new Error(errorText);
    return o;
}


function refreshNode(node, data) {
    if (node._rootHandler) {
        node._rootHandler.refresh(data);
    } else if (node.handler) {
        node.handler.refresh(data);
    }
}

/**
 * @param {string} url 
 * @returns {string:string} 
 */
function urlParameters(url) {
    assertNotNull(url, 'url is null');
    var urlParameters = {};
    var start = url.indexOf('?');
    if (start != -1) {
        var query = url.substring(start + 1);
        for (var keyValue of doSplit(query, '&')) {
            var param = doSplit(keyValue, '=');
            urlParameters[param[0]] = param[1];
        }
    }
    return urlParameters;
}


/**
 * Appends query parameters to the url. Also works in case the url 
 * already has query parameters.
 * 
 * @param {string} url 
 * @param {any} parameters key/value pairs as object 
 * @returns 
 */
function appendQueryParameters(url, parameters) {
    var result = url;
    var parameterNames = Object.keys(parameters);
    var parameterCount = parameterNames.length;
    if (parameterCount == 0) {
        return result;
    }
    if (result.indexOf('?') == -1) {
        result += '?';
    } else if (!result.endsWith('&')) {
        result += '&';
    }
    result += parameterNames.map(name => name + '=' + encodeURI(parameters[name])).join('&');
    return result;
}


/**
* @private
* @returns {string} the widget-id by leaving query from the widget-url
*/
function stripQuery(url) {
    if (url.indexOf('?') != -1) {
        return doSplit(url, '?')[0];
    }
    return url;
}


function randomString() {
    return Math.random().toString(36).slice(2);
}

function timeZone() {
    return Intl.DateTimeFormat().resolvedOptions().timeZone;
}


function cloneNode(node) {
    if (isElement(node)) {
        return this.cloneElement(node);
    } else {
        return this.cloneTextNode(node);
    }
}


function cloneElement(element) {
    var clone = document.createElement(element.localName);
    for (var name of element.getAttributeNames()) {
        clone.setAttribute(name, element.getAttribute(name));
    }
    // Attributes with variables are getting removed and treated
    // with a handler, instead
    if (element._removedAttributes) {
        for (var name of Object.keys(element._removedAttributes)) {
            clone.setAttribute(name, element._removedAttributes[name]);
        }
    }
    for (let index = 0; index < element.childNodes.length; index++) {
        const child = element.childNodes.item(index);
        clone.appendChild(this.cloneNode(child));
    }
    return clone;
}


function cloneTextNode(node) {
    return document.createTextNode(node.nodeValue);
}
