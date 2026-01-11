class TestApplication {

    constructor() {
        this.initializers = [];
        this.messageHandler = new MessageHandler();
        this.eventPublisher = new EventPublisher();
        this.sessionStorage = new SessionStore(this.eventPublisher);
        this.localStorage = new LocalStore(this.eventPublisher);
        this.clientStorage = new ClientStore(this.eventPublisher);
        this.httpConnector = new HttpConnectorMock();
        this.httpClient = new HttpClient(this.httpConnector);
        this.websocketConnector = this.createWebsocketConnectorIfPresent();
        if (this.websocketConnector) {
            this.websocketClient = new WebsocketClient(this.websocketConnector);
        }
        this.client = this.websocketClient ? this.websocketClient : this.httpClient;
        this.domAccessor = new DomAccessor();
        this.pages = new Pages(this.httpClient);
        this.urlResolver = new URLResolver(this.pages);
        this.widgetContainers = new WidgetContainers();
        this.widgets = new Widgets(this.httpClient);
        this.tagHandlers = new TagHandlers();
        this.includes = new Includes(this.httpClient);
        this.initializer = new Initializer(this.domAccessor, this.client, this.widgets, this.includes, this.widgetContainers, this.tagHandlers);
        this.pageController = new PageController(this.client, this.pages, this.initializer, this.urlResolver, this.tagHandlers);
        this.history = new PageHistory(this.pageController);
        this.globals = new GlobalStore(this.eventPublisher);
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
        // TODO Hier scheint es Probleme zu geben, wenn meherer Tests ausgeführt werden.
    }


    openPage(uri) {
        this.eventPublisher.publish(EventType.APP_INSTANCE_CREATED, this);
        document.location.pathname = uri;
        return this.httpClient.loadConfig()
            .then(config => this.pageController.setConfig(config))
            .then(config => this.widgetContainers.setConfig(config))
            .then(config => this.includes.loadIncludes(config))
            .then(config => this.widgets.loadWidgets(config))
            .then(config => this.pages.loadPages(config))
            .then(() => this.urlResolver.init())
            .then(() => this.pageController.displayPageForUrl(document.location.pathname))
            .then(() => { this.eventPublisher.publish(EventType.APP_INITIALIZED, app); })
            .catch(e => reportError(e));
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
        localStorage.reset();
        sessionStorage.reset();
    }

     createWebsocketConnectorIfPresent() {
        if (typeof WebsocketConnectorMock !== 'undefined') {
            return new WebsocketConnectorMock();
        }
        return null;
    }

}