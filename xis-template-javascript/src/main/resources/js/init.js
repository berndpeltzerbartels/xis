var html = getRootElement();
var xis = html._xis;
if (!xis) {
    var config = client.loadConfig();
    var pages = config.pages.map(pageId => loadPage(pageId));
    var widgets = config.widgets.map(pageId => loadWidget(pageId));
    xis = {
        config: config,
        pageId: welcomePageId,
        currentPageId: undefined,
        head: { childNodes: [] },
        pages: pages,
        widgets: widgets,
        getPage: pageId => this.pages.find(page => page.id == pageId),
        getWidget: widgetId => this.widgets.find(widget => widget.id == widgetId)
    }
    html._xis = xis;
}
if (!bindPage(document.location.pathname)) {
    bindPage(xis.welcomePageId);
}
refresh(html);
