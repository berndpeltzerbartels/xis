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

/**
 * Check if WebSocket connection is available
 * @returns {Promise<boolean>}
 */
function checkWebSocketAvailable() {
    return new Promise((resolve) => {
        if (typeof WebsocketConnectorMock === 'undefined') {
            resolve(true);

        } else {

        }
    });
}