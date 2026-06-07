/**
 * @param {Node} node
 * @returns {boolean}
 */
function isElement(node) {
    return node.nodeType == 1;
}

/**
 * @param {Node} node
 * @returns {boolean}
 */
function isDocumentFragment(node) {
    return node.nodeType == 11;
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

function normalizeElement(element) {
    return app.initializer.normalizeElement(element);
}

/**
 * @param {TagHandler} element 
 * @returns 
 */
function initializeElement(element) {
   return app.initializer.initialize(element);
}


function assertNotNull(o, errorText) {
    if (!errorText) errorText = 'Assertion failed. Value is null.'
    if (!o) throw new Error(errorText);
    return o;
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
* @returns {string} the frontlet-id by leaving query from the frontlet-url
*/
function stripQuery(url) {
    if (url.indexOf('?') != -1) {
        return doSplit(url, '?')[0];
    }
    return url;
}


function randomString(length = 24) {
    const alphabet = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    if (typeof crypto !== 'undefined' && crypto.getRandomValues) {
        const result = [];
        const maxAcceptedByte = Math.floor(256 / alphabet.length) * alphabet.length;
        while (result.length < length) {
            const bytes = new Uint8Array(length - result.length);
            crypto.getRandomValues(bytes);
            for (const byte of bytes) {
                if (byte < maxAcceptedByte) {
                    result.push(alphabet.charAt(byte % alphabet.length));
                    if (result.length === length) {
                        break;
                    }
                }
            }
        }
        return result.join('');
    }
    var value = '';
    while (value.length < length) {
        value += Math.random().toString(36).slice(2);
    }
    return value.slice(0, length);
}

function timeZone() {
    return Intl.DateTimeFormat().resolvedOptions().timeZone;
}


function cloneNode(node) {
    if (isElement(node)) {
        return cloneElement(node);
    } else {
        return cloneTextNode(node);
    }
}


function cloneElement(element) {
    var clone = document.createElement(element.localName);
    for (var name of element.getAttributeNames()) {
        clone.setAttribute(name, element.getAttribute(name));
    }
    for (let index = 0; index < element.childNodes.length; index++) {
        const child = element.childNodes.item(index);
        clone.appendChild(cloneNode(child));
    }
    return clone;
}


function cloneTextNode(node) {
    return document.createTextNode(node.nodeValue);
}

function handleError(error) {
    if (error && error.type === 'redirect') {
        return {redirected: true};
    }
    const msg = errorMessage(error);
    reportError('Unhandled error: ' + msg, error);
    throw error; // Fehler weiterwerfen, damit er nicht verschluckt wird
}

function errorMessage(error) {
    if (!error) {
        return 'unknown error';
    }
    if (error.message) {
        return error.message;
    }
    var httpMessage = httpErrorMessage(error);
    if (httpMessage) {
        return httpMessage;
    }
    if (error.target) {
        httpMessage = httpErrorMessage(error.target);
        if (httpMessage) {
            return httpMessage;
        }
    }
    if (error.type) {
        return error.type + ' event';
    }
    var text = String(error);
    if (text && text.indexOf('[object ') !== 0) {
        return text;
    }
    return 'unexpected ' + objectTypeName(error);
}

function httpErrorMessage(error) {
    if (!error) {
        return null;
    }
    var hasHttpFields = isSet(error.status) || isSet(error.statusText) || isSet(error.responseText);
    if (!hasHttpFields) {
        return null;
    }
    var responseMessage = responseErrorMessage(error.responseText);
    if (responseMessage) {
        return responseMessage;
    }
    var status = isSet(error.status) ? error.status : '?';
    var statusText = error.statusText ? ' ' + error.statusText : '';
    var responseUrl = error.responseURL || error.url;
    return responseUrl
        ? 'HTTP request failed (' + status + statusText + '): ' + responseUrl
        : 'HTTP request failed (' + status + statusText + ')';
}

function responseErrorMessage(responseText) {
    if (!responseText) {
        return null;
    }
    try {
        var parsed = JSON.parse(responseText);
        return parsed.message || parsed.error || responseText;
    } catch (e) {
        return responseText;
    }
}

function objectTypeName(error) {
    if (error.constructor && error.constructor.name) {
        return error.constructor.name;
    }
    return 'error';
}

function isSet(value) {
    return value !== undefined && value !== null;
}

function reportError(message, error)    {
    console.error(message, error);
    app.messageHandler.addErrorMessage(message);
}

function mergeObjects(object1, object2) {
    const result = {};
    if (!object1) object1 = {};
    if (!object2) object2 = {};
    for (const key of Object.keys(object1)) {
        result[key] = object1[key];
    }
    for (const key of Object.keys(object2)) {
        result[key] = object2[key];
    }
    return result;
}

function updateStores(response) {
   new StoreUpdater().updateStores(response);
   return Promise.resolve();
}
