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