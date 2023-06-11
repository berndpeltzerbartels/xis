class TestApplication {

    constructor(backendBridgeProvider) {
        this.httpClient = new HttpClientMock(backendBridgeProvider);
        this.refresher = new Refresher();
        this.domAccessor = new DomAccessor();
        this.client = new Client(this.httpClient);
        this.pages = new Pages(this.client);
        this.widgetContainers = new WidgetContainers();
        this.widgets = new Widgets(this.client);
        this.initializer = new Initializer(this.domAccessor, this.client, this.widgets, this.widgetContainers);
        this.pageController = new PageController(this.client, this.pages, this.initializer);
    }


    start() {
        var head = getElementByTagName('head');
        var body = getElementByTagName('body');
        this.initializer.initializeHtmlElement(head);
        this.initializer.initializeHtmlElement(body);
    }

    openPage(uri) {
        document.location.pathname = uri;
        var _this = this;
        return this.client.loadConfig()
            .then(config => _this.widgets.loadWidgets(config))
            .then(config => _this.pages.loadPages(config))
            .then(config => _this.pageController.displayInitialPage(config))
            .catch(e => console.error(error));
    }

}