class Application {

    constructor() {
        this.initializers = [];
        this.initializationListeners = [];
        this.messageHandler = window.messageHandler ?  window.messageHandler : new MessageHandler();
        this.clientState = new ClientState();
        this.localStorage = new LocalStore();
        this.httpConnector = new HttpConnector();
        this.domAccessor = new DomAccessor();
        this.client = new HttpClient(this.httpConnector);
        this.pages = new Pages(this.client);
        this.urlResolver = new URLResolver(this.pages);
        this.widgetContainers = new WidgetContainers();
        this.widgets = new Widgets(this.client);
        this.tagHandlers = new TagHandlers();
        this.elFunctions = new ELFunctions();
        this.initializer = new Initializer(this.domAccessor, this.client, this.widgets, this.widgetContainers, this.tagHandlers);
        this.pageController = new PageController(this.client, this.pages, this.initializer, this.urlResolver, this.tagHandlers);
        this.history = new PageHistory(this.pageController);
        this.backendService = new BackendService();
        this.eventPublisher = new EventPublisher();
        this.runInitializers();
    }

    /**
     * Runs all initializers that have been registered.
     * @private
     */
    runInitializers() {
        this.initializers.forEach(initializer => {
            initializer.initialize(this);
        });
    }


    start() {
        this.eventPublisher.publish(EventType.APP_INITIALIZED, this);
        this.client.loadConfig()
            .then(config => this.pageController.setConfig(config))
            .then(config => this.backendService.setConfig(config))
            .then(config => this.widgets.loadWidgets(config))
            .then(config => this.pages.loadPages(config))
            .then(() => this.pageController.displayPageForUrl(document.location.pathname + document.location.search))
            .catch(e => handleError(e));
    }

}