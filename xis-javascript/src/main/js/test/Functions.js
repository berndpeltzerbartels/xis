function openPage(uri) {
    return app.openPage(uri);
}

function reset() {
    app.reset();
    document.cookies = '';
}


function innerTextChanged(element) {
    element.innerTextChanged();
}

function nodeValueChanged(node) {
    node.nodeValueChanged();
}
