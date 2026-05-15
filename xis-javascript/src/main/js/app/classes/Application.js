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
        this.modals = new ModalManager(this.frontlets, this.initializer, this.tagHandlers);
        /** Serializes all render operations (page + frontlet refreshes) so they never overlap. */
        this.renderQueue = Promise.resolve();
        this.pageController = new PageController(this.client, this.pages, this.initializer, this.urlResolver, this.tagHandlers);
        this.history = new PageHistory(this.pageController);
        this.globals = new GlobalStore(this.eventPublisher);
        this.authenticationCheckRunning = false;
        this.lastAuthenticationCheckAt = 0;
        this.authenticationCheckIntervalMs = 5000;
        this.initializing = false;
        this.browserNavigationInProgress = false;
        this.installBrowserNavigationListener();
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

    installBrowserNavigationListener() {
        if (typeof window === 'undefined' || typeof window.addEventListener !== 'function') {
            return;
        }
        window.addEventListener('pagehide', () => this.prepareForBrowserNavigation());
        window.addEventListener('beforeunload', () => this.prepareForBrowserNavigation());
    }

    prepareForBrowserNavigation() {
        this.browserNavigationInProgress = true;
        if (this.eventConnector && typeof this.eventConnector.closeForBrowserNavigation === 'function') {
            this.eventConnector.closeForBrowserNavigation();
        }
    }

    isBrowserNavigationInProgress() {
        return this.browserNavigationInProgress;
    }


    start() {
        this.eventPublisher.publish(EventType.APP_INSTANCE_CREATED, this);
        this.initializing = true;
        this.connectEventSources(null);
        this.httpClient.loadConfig()
            .then(config => this.connectEventSources(config))
            .then(config => this.pageController.setConfig(config))
            .then(config => {
                if (this.eventConnector && typeof this.eventConnector.setPendingEventTtlMs === 'function') {
                    this.eventConnector.setPendingEventTtlMs(config.pendingEventTtlSeconds * 1000);
                }
                return config;
            })
            .then(config => this.frontletContainers.setConfig(config))
            .then(config => this.includes.loadIncludes(config))
            .then(config => this.frontlets.loadFrontlets(config))
            .then(config => this.pages.loadPages(config))
            .then(() => this.pageController.displayPageForUrl(document.location.pathname + document.location.search))
            .then(() => this.setupLinkInterceptor())
            .then(() => {
                this.initializing = false;
                this.eventPublisher.publish(EventType.APP_INITIALIZED, this);
            })
            .catch(e => {
                this.initializing = false;
                handleError(e);
            });
    }

    checkAuthenticationAfterSseError() {
        const now = Date.now();
        if (this.authenticationCheckRunning || now - this.lastAuthenticationCheckAt < this.authenticationCheckIntervalMs) {
            return;
        }
        if (!this.pageController || !this.pageController.resolvedURL) {
            return;
        }
        this.authenticationCheckRunning = true;
        this.lastAuthenticationCheckAt = now;
        this.pageController.refreshCurrentPage()
            .catch(error => handleError(error))
            .finally(() => this.authenticationCheckRunning = false);
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

            // Skip if link already has XIS handler (xis:page, xis:frontlet, xis:action)
            if (link.hasAttribute('xis:page') ||
                link.hasAttribute('xis:frontlet') ||
                link.hasAttribute('xis:action') ||
                link.hasAttribute('xis:modal')) {
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
            return new SseConnector(clientId);
        }
        return null;
    }

    connectEventSources(config) {
        if (this.eventConnector && typeof this.eventConnector.connectAll === 'function') {
            this.eventConnector.connectAll(this.sseEndpointUrls(config))
                .catch(e => reportError('[SSE] error connecting event source', e));
        }
        return config;
    }

    sseEndpointUrls(config) {
        const endpoints = [this.sseEndpointUrl()];
        if (config) {
            this.addSseEndpointUrls(endpoints, config.pageAttributes);
            this.addSseEndpointUrls(endpoints, config.frontletAttributes);
        }
        return [...new Set(endpoints)];
    }

    addSseEndpointUrls(endpoints, attributesById) {
        if (!attributesById) {
            return;
        }
        for (const attributes of Object.values(attributesById)) {
            if (attributes && attributes.host) {
                endpoints.push(this.sseEndpointUrlForHost(attributes.host));
            }
        }
    }

    sseEndpointUrlForHost(host) {
        if (host.endsWith('/')) {
            return host.substring(0, host.length - 1) + "/xis/events";
        }
        return host + "/xis/events";
    }

    sseEndpointUrl() {
        if (window.location && window.location.origin) {
            return window.location.origin + "/xis/events";
        }
        const protocol = window.location.protocol === 'https:' ? 'https://' : 'http://';
        return protocol + window.location.host + "/xis/events";
    }

    clientId() {
        var clientId = localStorage.getItem('xis.clientId');
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
