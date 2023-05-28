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
 * 
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

/**
 * 
 * @param {string} uri 
 * @returns {Promise<void>}
 */
function bindPage(uri) {
    return pageController.bindPage(uri);
}

function reloadDataAndRefreshCurrentPage() {
    return new Promise((resolve, _) => {
        pageController.reloadDataAndRefreshCurrentPage();
        resolve();
    });
}

function initializeElement(element) {
    initializer.initialize(element);
}

function assertNotNull(o, errorText) {
    if (o) return;
    else throw new Error(errorText);
}


function refreshNode(node, data) {
    refresher.refreshNode(node, data);
}

