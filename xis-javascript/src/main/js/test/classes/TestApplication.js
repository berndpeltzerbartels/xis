class TestApplication {

    constructor() {
        this.html = new PageHtml();
        this.httpClient = new HttpClientMock();
        this.refresher = new Refresher();
        this.domAccessor = new DomAccessor();
        this.client = new Client(this.httpClient);
        this.pages = new Pages(this.client);
        this.urlResolver = new URLResolver(this.pages);
        this.widgetContainers = new WidgetContainers();
        this.widgets = new Widgets(this.client);
        this.initializer = new Initializer(this.domAccessor, this.client, this.widgets, this.widgetContainers);
        this.pageController = new PageController(this.client, this.pages, this.initializer, this.urlResolver, this.html);
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
            .then(config => { config.welcomePageId = uri; return config; })
            .then(config => this.pageController.setConfig(config))
            .then(() => _this.pageController.displayPageForUrl(document.location.pathname))
            .catch(e => console.error(e));
    }

    reset() {
        this.html.reset();
        this.pageController.reset();
        this.pages.reset();
        this.widgets.reset();
        this.widgetContainers.reset();
    }

}