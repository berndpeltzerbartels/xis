class TestApplication {

    constructor() {
        this.clientState = new ClientState();
        this.localStorage = new LocalStore();
        this.httpConnector = new HttpConnectorMock();
        this.domAccessor = new DomAccessor();
        this.client = new HttpClient(this.httpConnector);
        this.pages = new Pages(this.client);
        this.urlResolver = new URLResolver(this.pages);
        this.widgetContainers = new WidgetContainers();
        this.widgets = new Widgets(this.client);
        this.tagHandlers = new TagHandlers();
        this.initializer = new Initializer(this.domAccessor, this.client, this.widgets, this.widgetContainers, this.tagHandlers);
        this.pageController = new PageController(this.client, this.pages, this.initializer, this.urlResolver, this.tagHandlers);
        this.backendService = new BackendService();
    }


    start() {
        // Noop
    }

    openPage(uri) {
        document.location.pathname = uri;
        var _this = this;
        return this.client.loadConfig()
            .then(config => _this.pageController.setConfig(config))
            .then(config => _this.backendService.setConfig(config))
            .then(config => _this.widgets.loadWidgets(config))
            .then(config => _this.pages.loadPages(config))
            .then(() => _this.urlResolver.init())
            .then(() => _this.pageController.displayPageForUrlLater(document.location.pathname))
            .catch(e => console.error(e));
    }

    updateWelcomePage(uri, config) {
        return new Promise((resolve, _) => {
            config.welcomePageId = uri;
            resolve(config);
        });
    }

    reset() {
        this.pageController.reset();
        this.pages.reset();
        this.widgets.reset();
        this.widgetContainers.reset();
    }

}