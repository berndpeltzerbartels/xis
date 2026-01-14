class Application {

    constructor() {
        this.initializers = [];
        this.initializationListeners = [];
        this.messageHandler = window.messageHandler ?  window.messageHandler : new MessageHandler();
        this.eventPublisher = new EventPublisher();
        this.sessionStorage = new SessionStore(this.eventPublisher);
        this.localStorage = new LocalStore(this.eventPublisher);
        this.clientStorage = new ClientStore(this.eventPublisher);
        this.clientId = this.clientId();
        this.httpConnector = new HttpConnector(this.clientId);
        this.httpClient = new HttpClient(this.httpConnector, this.clientId);
        this.httpClient.clientId = this.clientId;
        this.websocketConnector = this.createWebsocketConnectorIfPresent(this.clientId);
        if (this.websocketConnector) {
            this.websocketClient = new WebsocketClient(this.websocketConnector, this.clientId);
            this.websocketClient.clientId = this.clientId;
        }
        this.client = this.websocketClient ? this.websocketClient : this.httpClient;
        this.domAccessor = new DomAccessor();
        this.pages = new Pages(this.httpClient);
        this.urlResolver = new URLResolver(this.pages);
        this.widgetContainers = new WidgetContainers();
        this.widgets = new Widgets(this.httpClient);
        this.tagHandlers = new TagHandlers();
        this.elFunctions = new ELFunctions();
        this.includes = new Includes(this.httpClient);
        this.initializer = new Initializer(this.domAccessor, this.httpClient, this.widgets, this.includes, this.widgetContainers, this.tagHandlers);
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
        this.eventPublisher.publish(EventType.APP_INSTANCE_CREATED, this);
        this.httpClient.loadConfig()
            .then(config => this.pageController.setConfig(config))
            .then(config => { if (this.websocketClient) { this.websocketClient.setConfig(config); } return config; })
            .then(config => this.widgetContainers.setConfig(config))
            .then(config => this.includes.loadIncludes(config))
            .then(config => this.widgets.loadWidgets(config))
            .then(config => this.pages.loadPages(config))
            .then(() => this.pageController.displayPageForUrl(document.location.pathname + document.location.search))
            .then(() => this.setupLinkInterceptor())
            .then(() => this.eventPublisher.publish(EventType.APP_INITIALIZED, this))
            .catch(e => handleError(e));
    }

    /**
     * Sets up global link interceptor to prevent full page reloads for internal links.
     * Intercepts all <a href> clicks and routes internal links through the PageController.
     * @private
     */
    setupLinkInterceptor() {
        document.addEventListener('click', (event) => {
            // Find the closest anchor element
            const link = event.target.closest('a[href]');
            if (!link) return;

            // Skip if link already has XIS handler (xis:page, xis:widget, xis:action)
            if (link.hasAttribute('xis:page') ||
                link.hasAttribute('xis:widget') ||
                link.hasAttribute('xis:action')) {
                return;
            }

            const href = link.getAttribute('href');

            // Only intercept internal links
            if (this.isInternalLink(href)) {
                event.preventDefault();
                this.pageController.displayPageForUrl(href);
            }
        });
    }

    /**
     * Checks if a link is internal (same-origin, not external protocol).
     * @private
     * @param {string} href - The href attribute value
     * @returns {boolean} True if the link is internal
     */
    isInternalLink(href) {
        if (!href) return false;

        // External protocols
        if (href.startsWith('http://') ||
            href.startsWith('https://') ||
            href.startsWith('//') ||
            href.startsWith('mailto:') ||
            href.startsWith('tel:') ||
            href.startsWith('javascript:')) {
            return false;
        }

        // Hash-only links (same page anchor)
        if (href.startsWith('#')) {
            return false;
        }

        // Download links
        if (href.startsWith('data:') || href.startsWith('blob:')) {
            return false;
        }

        return true;
    }

    createWebsocketConnectorIfPresent(clientId) {
        if (typeof WebsocketConnector !== 'undefined' &&  typeof WebSocket !== 'undefined') {
            var connector = new WebsocketConnector(clientId);
            connector.connect("ws://localhost:8080/ws");
            return connector;
        }
        return null;
    }

    clientId() {
        var clientId = localStorage .getItem('xis.clientId');
        if (!clientId) {
            clientId = randomString();
            localStorage.setItem('xis.clientId', clientId);
        }
        return clientId;
    }

}