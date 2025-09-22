function openPage(uri) {

}

function htmlToElement(html) {
    var holder = document.createElement('div');
    holder.innerHTML = html;
    for (var child of nodeListToArray(holder.childNodes)) {
        if (isElement(child)) {
            return child;
        }
    }
}


function readHeadChildArray(content) {
    const parser = new DOMParser();
    const doc = parser.parseFromString(content, 'text/html');
    const head = doc.head;
    return head ? Array.from(head.children) : [];
}



