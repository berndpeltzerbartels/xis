function openPage(uri) {
    return app.openPage(uri);
}

function reset() {
    app.reset();
    document.cookies = '';
}


function readHeadChildArray(html) {
    var arr = [];
    var holder = document.createElement('div');
    holder.innerHTML = html;
    for (var child of nodeListToArray(holder.childNodes)) {
        if (isElement(child)) {
            arr.push(child);
        }
    }
    return arr;
}

function readBodyChildArray(html) {
    var arr = [];
    var holder = document.createElement('div');
    holder.innerHTML = html;
    for (var child of nodeListToArray(holder.childNodes)) {
        if (isElement(child)) {
            arr.push(child);
        }
    }
    return arr;
}

function isFloat(n) {
    return true;
}

function isInt(n) {
    return true;
}

