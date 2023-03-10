var client = new Client(new HttpClient());

function preload(config) {

}


client.loadConfig(config => {


});


client.loadConfig(config => {
    var html = getRootElement();
    var xis = {
        config: config,
        page: undefined,
        currentPageId: undefined,
        head: { childNodes: [] },
        pages: config.pageConfig.map(page => page.id).map(pageId => loadPage(pageId)),
        widgets: config.widgetIds.map(pageId => loadWidget(pageId)),
        getPage: pageId => this.pages.find(page => page.id == pageId),
        getWidget: widgetId => this.widgets.find(widget => widget.id == widgetId)
    }
    html._xis = xis;

    var page = xis.getPage(document.location.pathname);
    if (!page) {
        page = xis.getPage(xis.config.welcomePageId);
    }

    var pageConfig = client.getConfig().pages[page.id];
    if (xis.page && xis.page != page && pageConfig['destroy']) {
        client.onPageDestroy(xis.page, xis.data);
    }
    if (pageConfig['init'] && !page.initialized) {
        client.onInitPage(page, data, newData => {
            refresh(html, new Data(newData, data));
            page.initialized = true;
        });
    } else if (pageConfig['show']) {
        client.onShowPage(page, data, newData => refresh(html, new Data(newData, data)));
    } else {
        refresh(html, data);
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


});