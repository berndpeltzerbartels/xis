class TestApplication {

    constructor() {
        this.clientState = new ClientState();
        this.localStorage = new LocalStore();
        this.httpConnector = new HttpConnectorMock();
        this.domAccessor = new DomAccessor();
        this.tokenManager = new TokenManager();
        this.client = new HttpClient(this.httpConnector, this.tokenManager);
        this.pages = new Pages(this.client);
        this.urlResolver = new URLResolver(this.pages);
        this.widgetContainers = new WidgetContainers();
        this.widgets = new Widgets(this.client);
        this.tagHandlers = new TagHandlers();
        this.initializer = new Initializer(this.domAccessor, this.client, this.widgets, this.widgetContainers, this.tagHandlers);
        this.pageController = new PageController(this.client, this.pages, this.initializer, this.urlResolver, this.tagHandlers);
        this.history = new PageHistory(this.pageController);
        this.backendService = new BackendService();
    }


    start() {
        // TODO Hier scheint es Probleme zu geben, wenn meherer Tests ausgeführt werden.
    }


    openPage(uri) {
        document.location.pathname = uri;
        this.tokenManager.init();
        return this.client.loadConfig()
            .then(config => this.pageController.setConfig(config))
            .then(config => this.backendService.setConfig(config))
            .then(config => this.widgets.loadWidgets(config))
            .then(config => this.pages.loadPages(config))
            .then(() => this.urlResolver.init())
            .then(() => this.pageController.displayPageForUrl(document.location.pathname))
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
        this.tokenManager.reset();
    }

}