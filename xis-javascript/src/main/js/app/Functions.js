function openPage(uri) {

}

function htmlToElement(html) {
    var holder = document.createElement('holder');
    holder.innerHTML = html;
    for (var child of nodeListToArray(holder.childNodes)) {
        if (isElement(child)) {
            return child;
        }
    }
}

function innerTextChanged(element) {
    // Not needed here, but in testcode
}

function nodeValueChanged(node) {
    // Not needed here, but in testcode
}

