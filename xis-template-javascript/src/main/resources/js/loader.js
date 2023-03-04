function showPage(pageId) {
    var pageStatus = getRootNode()._xis.page;
    if (pageId != pageStatus.pageId) {
        if (pageStatus.pageId) {
            unloadPage();
            pageStatus.pageId = undefined;
        }
        loadPage(pageId);
        pageStatus.pageId = pageId;
    }
}

function refreshWidget(container, xis) {
    var widget = xis.widget;
    if (widget.currentWidgetId != widget.widgetId) {
        unloadWidget(container);
        widget.currentWidgetId = undefined;
        loadWidget(container, widgetId);
        widget.currentWidgetId = widget.widgetId;
        return true;
    }
    return false;
}

function loadPage(pageId) {
    var shadowHead = document.createElement('head');
    var shadowBody = document.createElement('body');
    shadowHead.innerHTML = client.loadHead(pageId);
    shadowBody.innerHTML = client.loadBody(pageId);
    var html = getTemplateRoot();
    var head = getTemplateHead();
    var body = getTemplateBody();
    var title = getTitle();
    var xis = html._xis;
    for (var i = 0; i < shadowHead.childNodes.length; i++) {
        var child = shadowHead.childNodes.item(i);
        if (child.localName && child.localName == title) {
            title.innerHTML = child.innerHTML;
        } else {
            head.appendChild(child);
            xis.head.childNodes.push(child);
        }
    }
    for (var i = 0; i < shadowBody.childNodes.length; i++) {
        body.appendChild(shadowHead.childNodes.item(i));
    }
    for (var name of shadowBody.getAttributeNames()) {
        body.setAttribute(name, shadowBody.getAttribute(name));
    }
}


function unloadPage() {
    var html = getTemplateRoot();
    var xis = html._xis;
    var head = getTemplateHead();
    var body = getTemplateBody();
    var title = getTitle();
    title.innerHTML = '';
    // We do not want to remove our script-tags etc.
    for (var i = 0; i < xis.head.childNodes.length; i++) {
        var child = xis.head.childNodes[i];
        head.removeChild(child);
    }
    for (var i = 0; i < body.childNodes.length; i++) {
        var child = body.childNodes.item(i);
        body.removeChild(child);
    }
    for (var name of body.getAttributeNames()) {
        body.removeAttribute(name);
    }
}

function loadWidget(container, widgetId, client) {
    container.innerHTML = client.loadWidget(widgetId);
}

function unloadWidget(container) {
    if (container._xis) {
        for (var i = 0; i < container.childNodes.length; i++) {
            var child = container.childNodes.item(i);
            container.removeChild(child);
        }
        container._xis = undefined;
    }
}


function getTemplateHead() {
    return getElementByTagName('head');
}

function getTemplateBody() {
    return getElementByTagName('body');
}

function getTemplateRoot() {
    return document.getRootNode();
}

function getElementByTagName(name) {
    return document.getElementsByTagName(name).item(0);
}





