class Application {

    constructor() {
        this.clientState = new ClientState();
        this.localStorage = new LocalStore();
        this.httpConnector = new HttpConnector();
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
        this.backendService = new BackendService();
    }


    start() {
        this.tokenManager.init();
        this.client.loadConfig()
            .then(config => this.pageController.setConfig(config))
            .then(config => this.backendService.setConfig(config))
            .then(config => this.widgets.loadWidgets(config))
            .then(config => this.pages.loadPages(config))
            .then(() => this.pageController.displayPageForUrlLater(document.location.pathname))
            .catch(e => console.error(e));
    }

}