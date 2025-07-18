function openPage(uri) {
    return app.openPage(uri);
}

function reset() {
    app.reset();
    document.cookies = '';
}

function setAccessToken(token) {
    app.tokenManager.setAccessToken(token);
}

function innerTextChanged(element) {
    element.innerTextChanged();
}

function nodeValueChanged(node) {
    node.nodeValueChanged();
}
