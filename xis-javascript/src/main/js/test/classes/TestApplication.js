class TestApplication {

    constructor() {
        this.initializers = [];
        this.messageHandler = new MessageHandler();
        this.eventPublisher = new EventPublisher();
        this.sessionStorage = new SessionStore(this.eventPublisher);
        this.localStorage = new LocalStore(this.eventPublisher);
        this.clientStorage = new ClientStore(this.eventPublisher);
        this.clientId = this.clientId();
        this.httpConnector = new HttpConnectorMock(this.clientId);
        this.httpClient = new HttpClient(this.httpConnector, this.clientId);
        this.eventConnector = this.createEventConnectorIfPresent(this.clientId);
        this.client = this.httpClient;
        this.domAccessor = new DomAccessor();
        this.pages = new Pages(this.httpClient);
        this.urlResolver = new URLResolver(this.pages);
        this.frontletContainers = new FrontletContainers();
        this.frontlets = new Frontlets(this.httpClient);
        this.tagHandlers = new TagHandlers();
        this.includes = new Includes(this.httpClient);
        this.initializer = new Initializer(this.domAccessor, this.client, this.frontlets, this.includes, this.frontletContainers, this.tagHandlers);
        /** Serializes all render operations (page + widget refreshes) so they never overlap. */
        this.renderQueue = Promise.resolve();
        this.pageController = new PageController(this.client, this.pages, this.initializer, this.urlResolver, this.tagHandlers);
        this.history = new PageHistory(this.pageController);
        this.globals = new GlobalStore(this.eventPublisher);
        this.runInitializers();
    }


    clientId() {
        var clientId = localStorage .getItem('xis.clientId');
        if (!clientId) {
            clientId = randomString();
            localStorage.setItem('xis.clientId', clientId);
        }
        return clientId;
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
        // TODO There seem to be issues when multiple tests are executed.
    }


    openPage(uri) {
        this.eventPublisher.publish(EventType.APP_INSTANCE_CREATED, this);
        document.location.pathname = uri;
        return this.httpClient.loadConfig()
            .then(config => this.pageController.setConfig(config))
            .then(config => {
                if (this.eventConnector && typeof this.eventConnector.setPendingEventTtlMs === 'function') {
                    this.eventConnector.setPendingEventTtlMs(config.pendingEventTtlSeconds * 1000);
                }
                return config;
            })
            .then(config => this.frontletContainers.setConfig(config))
            .then(config => this.includes.loadIncludes(config))
            .then(config => this.frontlets.loadWidgets(config))
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
        this.frontlets.reset();
        this.frontletContainers.reset();
        localStorage.reset();
        sessionStorage.reset();
    }

     createEventConnectorIfPresent(clientId) {
        return new TestEventConnector(clientId);
    }

    submitForm(id, action) {
        const form = document.getElementById(id);
        if (form) {
            const formHandler = this.tagHandlers.getHandler(form);
            if (formHandler && formHandler.type === 'form-handler') {
                formHandler.submit(action);
            } else {
                console.error('No form handler found for form with id: ' + id);
            }
        } else {
            console.error('No form found with id: ' + id);
        }
    }

}

class TestEventConnector {

    constructor(clientId) {
        this.clientId = clientId;
    }

    setPendingEventTtlMs(_) {
    }

    simulatePushEvent(updateEventKey) {
        return app.pageController.handleUpdateEvents([updateEventKey])
            .then(pageUpdated => {
                if (!pageUpdated) {
                    return app.frontletContainers.handleUpdateEvents([updateEventKey]);
                }
                return pageUpdated;
            })
            .catch(e => reportError('TestEventConnector failed for update-event key=' + updateEventKey, e));
    }
}
