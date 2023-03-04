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
    var headHolder = document.createElement('div');
    var bodyHolder = document.createElement('div');
    headHolder.innerHTML = client.loadHead(pageId);
    bodyHolder.innerHTML = client.loadBody(pageId);
    return {
        id: pageId,
        headHolder: headHolder,
        bodyHolder, bodyHolder,
        getHeadElement: () => this.headHolder.childNodes.item(0),
        getBodyElement: () => this.bodyHolder.childNodes.item(0)
    };
}

function loadWidget(widgetId) {
    var holder = document.createElement('div');
    return {
        id: widgetId,
        holder: holder,
        getRootElement: () => holder.childNodes.item(0)
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





