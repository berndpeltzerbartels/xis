
function append(orig, value) {
    orig += value;
}

function createElement(tagName, attributes = {}) {
    var e = document.createElement(tagName);
    for (var name of Object.keys(attributes)) {
        e.setAttribute(name, attributes[name]);
    }
    return e;
}

function createTextNode() {
    return document.createTextNode('');
}

