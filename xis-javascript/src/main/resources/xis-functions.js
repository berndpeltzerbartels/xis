
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

function byId(id) {
    return document.getElementById(id);
}
