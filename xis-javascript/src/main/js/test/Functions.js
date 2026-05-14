function openPage(uri) {
    return app.openPage(uri);
}

function reset() {
    app.reset();
    document.cookies = '';
}

/**
 * Simulates a server-push update-event in tests.
 * Requires a test connector mock to be present on the classpath.
 * @param {string} updateEventKey
 */
function simulatePushEvent(updateEventKey) {
    const connector = app.eventConnector;
    if (!connector || typeof connector.simulatePushEvent !== 'function') {
        throw new Error('simulatePushEvent requires a push-event test connector on the test classpath');
    }
    return connector.simulatePushEvent(updateEventKey);
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
