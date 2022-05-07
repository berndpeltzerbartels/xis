function setAttributes(element, attributes) {
    var keys = Object.keys(attributes);
    for (var i = 0; i < keys.length; i++) {
        var name = keys[i];
        element.setAttribute(name, attributes[name]);
    }
}