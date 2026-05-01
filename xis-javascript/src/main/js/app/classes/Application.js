class SseConnector {

    constructor(clientId) {
        this.clientId = clientId;
        this.eventSource = null;
        this.connected = false;
        this.url = null;
    }

    connect(url) {
        this.url = appendQueryParameters(url, { clientId: this.clientId });
        if (this.eventSource) {
            this.close();
        }
        this.eventSource = new EventSource(this.url);
        this.eventSource.onopen = () => {
            this.connected = true;
            console.debug('[SSE] connected to ' + this.url);
        };
        this.eventSource.onmessage = (event) => {
            this.handleMessage(event.data);
        };
        this.eventSource.onerror = () => {
            this.connected = false;
            console.warn('[SSE] connection error for ' + this.url);
        };
        return this;
    }

    close() {
        if (this.eventSource) {
            this.eventSource.close();
            this.eventSource = null;
        }
        this.connected = false;
    }

    isConnected() {
        return this.connected && this.eventSource !== null;
    }

    handleMessage(data) {
        const eventKey = typeof data === 'string' ? data.trim() : '';
        if (!eventKey) {
            return;
        }
        console.debug('[SSE] update-event: key=' + eventKey);
        app.pageController.handleUpdateEvents([eventKey])
            .then(pageUpdated => {
                if (!pageUpdated) {
                    app.frontletContainers.handleUpdateEvents([eventKey]);
                }
            })
            .catch(e => reportError('[SSE] error handling update-event key=' + eventKey, e));
    }
}

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
        this.eventConnector = this.createEventConnectorIfSupported(this.clientId);
        this.client = this.httpClient;
        this.domAccessor = new DomAccessor();
        this.pages = new Pages(this.httpClient);
        this.urlResolver = new URLResolver(this.pages);
        this.frontletContainers = new FrontletContainers();
        this.frontlets = new Frontlets(this.httpClient);
        this.tagHandlers = new TagHandlers();
        this.elFunctions = new ELFunctions();
        this.includes = new Includes(this.httpClient);
        this.initializer = new Initializer(this.domAccessor, this.httpClient, this.frontlets, this.includes, this.frontletContainers, this.tagHandlers);
        /** Serializes all render operations (page + widget refreshes) so they never overlap. */
        this.renderQueue = Promise.resolve();
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

    createEventConnectorIfSupported(clientId) {
        if (typeof EventSource !== 'undefined') {
            var connector = new SseConnector(clientId);
            connector.connect(this.sseEndpointUrl());
            return connector;
        }
        return null;
    }

    sseEndpointUrl() {
        if (window.location && window.location.origin) {
            return window.location.origin + "/xis/events";
        }
        const protocol = window.location.protocol === 'https:' ? 'https://' : 'http://';
        return protocol + window.location.host + "/xis/events";
    }

    clientId() {
        var clientId = localStorage .getItem('xis.clientId');
        if (!clientId) {
            clientId = randomString();
            localStorage.setItem('xis.clientId', clientId);
        }
        return clientId;
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
