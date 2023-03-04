var config = client.loadConfig()
var welcomePageId = config.welcomePage.id; // add url ?
var xis = {
    page: {
        pageId: welcomePageId,
        currentPageId: undefined,
        head: { childNodes: [] }
    }
};
getRootElement()._xis = xis;
showPage(welcomePageId);
