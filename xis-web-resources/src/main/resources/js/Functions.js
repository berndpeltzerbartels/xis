/**
 * @param {Node} node
 * @returns {boolean}
 */
function isElement(node) {
    var rv = node.childNodes !== null && node.childNodes !== undefined;
    console.log('isElement' + node + ' => ' + rv);
    return rv;
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

function traceMethodCalls(obj) {
    const handler = {
        get(target, propKey, receiver) {
            console.log('get(..)');
            const targetValue = Reflect.get(target, propKey, receiver);
            console.log('type: ' + (typeof targetValue));
            if (typeof targetValue === 'function') {
                return function (...args) {
                    console.log('CALL', propKey, args);
                    return targetValue.apply(this, args); // (A)
                }
            } else {
                return targetValue;
            }
        }
    };
    return new Proxy(obj, handler);
}