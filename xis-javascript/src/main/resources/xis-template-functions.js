
function createElement(tagName, attributes) {
    if (!attributes) attributes = {};
    var e = document.createElement(tagName);
    var keys = Object.keys(attributes);
    for (var i = 0; i < keys.length; i++) {
        var name = keys[i];
        e.setAttribute(name, attributes[name]);
    }
    return e;
}

function createTextNode(content) {
    return document.createTextNode(content);
}

