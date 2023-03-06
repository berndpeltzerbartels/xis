var html = getRootElement();
var xis = html._xis;
if (!xis) {
    var config = client.loadConfig();
    var pages = config.pageIds.map(pageId => loadPage(pageId));
    var widgets = config.widgetIds.map(pageId => loadWidget(pageId));
    var client = new Client(config);
    xis = {
        config: config,
        page: undefined,
        currentPageId: undefined,
        head: { childNodes: [] },
        pages: pages,
        widgets: widgets,
        getPage: pageId => this.pages.find(page => page.id == pageId),
        getWidget: widgetId => this.widgets.find(widget => widget.id == widgetId)
    }
    html._xis = xis;
}

var page = xis.getPage(document.location.pathname);
if (!page) {
    page = xis.getPage(xis.config.welconePageId);
}

var data;
if (xis.page != page) {
    if (xis.page) {
        client.onPageDestroy(xis.page, xis.data);
    }
    if (page.initialized) {
        data = client.onInitPage(page);
        page.initialized = true;
    }
    bindPage(page);
    xis.page = page;
}
if (!data) {
    data = client.Page(page);
}
xis.data = data;
refresh(html, data);


