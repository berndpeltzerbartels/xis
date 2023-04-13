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