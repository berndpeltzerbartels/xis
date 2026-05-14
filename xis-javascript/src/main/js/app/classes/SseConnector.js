class SseConnector {

    constructor(clientId) {
        this.clientId = clientId;
        this.eventSource = null;
        this.eventSources = {};
        this.connected = false;
        this.endpointUrl = null;
        this.endpointUrls = [];
        this.url = null;
        this.connectionPromises = {};
        this.reconnectTimers = {};
        this.reconnectDelayMs = 1000;
        this.maxReconnectDelayMs = 30000;
        this.reconnectAttempts = {};
        this.pendingEventTtlMs = 0;
        this.browserNavigationInProgress = false;
    }

    connect(url) {
        return this.connectAll([url]);
    }

    connectAll(urls) {
        const nextEndpointUrls = [...new Set(urls.filter(url => url))];
        for (const endpointUrl of Object.keys(this.eventSources)) {
            if (!nextEndpointUrls.includes(endpointUrl)) {
                this.closeEndpoint(endpointUrl);
            }
        }
        this.endpointUrls = nextEndpointUrls;
        this.endpointUrl = nextEndpointUrls[0] || null;
        for (const endpointUrl of nextEndpointUrls) {
            this.connectEndpointIfNeeded(endpointUrl);
        }
        this.eventSource = this.endpointUrl ? this.eventSources[this.endpointUrl] : null;
        this.connected = Object.keys(this.eventSources).length > 0;
        return Promise.all(nextEndpointUrls.map(endpointUrl => this.connectionPromiseFor(endpointUrl)))
            .then(() => this);
    }

    connectEndpointIfNeeded(endpointUrl) {
        if (this.browserNavigationInProgress) {
            return Promise.resolve(false);
        }
        const eventSource = this.eventSources[endpointUrl];
        if (eventSource && !this.isClosed(eventSource)) {
            return this.connectionPromiseFor(endpointUrl);
        }
        return this.connectEndpoint(endpointUrl);
    }

    connectEndpoint(endpointUrl) {
        if (this.browserNavigationInProgress) {
            return Promise.resolve(false);
        }
        this.closeEndpoint(endpointUrl, false);
        const eventUrl = appendQueryParameters(endpointUrl, { clientId: this.clientId });
        this.url = eventUrl;
        const eventSource = new EventSource(eventUrl, { withCredentials: true });
        this.eventSources[endpointUrl] = eventSource;
        const connectionPromise = new Promise(resolve => {
            const timeout = setTimeout(() => {
                if (this.eventSources[endpointUrl] !== eventSource) {
                    resolve(false);
                    return;
                }
                if (this.shouldIgnoreConnectionError(endpointUrl, eventSource)) {
                    resolve(false);
                    return;
                }
                if (this.isOpen(eventSource)) {
                    this.markConnected(endpointUrl, eventUrl);
                    resolve(true);
                    return;
                }
                console.warn('[SSE] connection timeout for ' + eventUrl);
                this.scheduleReconnect(endpointUrl);
                this.requestAuthenticationCheck(endpointUrl);
                resolve(false);
            }, 5000);
            eventSource.onopen = () => {
                clearTimeout(timeout);
                this.markConnected(endpointUrl, eventUrl);
                resolve(true);
            };
            eventSource.onerror = () => {
                if (this.shouldIgnoreConnectionError(endpointUrl, eventSource)) {
                    clearTimeout(timeout);
                    resolve(false);
                    return;
                }
                this.connected = Object.values(this.eventSources).some(source => source.readyState === 1);
                console.warn('[SSE] connection error for ' + eventUrl);
                clearTimeout(timeout);
                this.closeEndpoint(endpointUrl, false);
                this.scheduleReconnect(endpointUrl);
                this.requestAuthenticationCheck(endpointUrl);
                resolve(false);
            };
        }).finally(() => {
            if (this.connectionPromises[endpointUrl] === connectionPromise) {
                delete this.connectionPromises[endpointUrl];
            }
        });
        this.connectionPromises[endpointUrl] = connectionPromise;
        eventSource.onmessage = (event) => {
            this.handleMessage(event.data);
        };
        return this.connectionPromises[endpointUrl];
    }

    markConnected(endpointUrl, eventUrl) {
        this.connected = true;
        this.reconnectAttempts[endpointUrl] = 0;
        console.debug('[SSE] connected to ' + eventUrl);
    }

    ensureConnected() {
        if (this.browserNavigationInProgress || !this.endpointUrls || this.endpointUrls.length === 0) {
            return Promise.resolve();
        }
        const promises = [];
        for (const endpointUrl of this.endpointUrls) {
            const eventSource = this.eventSources[endpointUrl];
            if (!eventSource || this.isClosed(eventSource)) {
                promises.push(this.connectEndpoint(endpointUrl));
            } else if (this.isOpen(eventSource)) {
                promises.push(Promise.resolve(true));
            } else {
                promises.push(this.connectionPromiseFor(endpointUrl));
            }
        }
        return Promise.all(promises);
    }

    connectionPromiseFor(endpointUrl) {
        return this.connectionPromises[endpointUrl] || Promise.resolve(this.isOpen(this.eventSources[endpointUrl]));
    }

    isClosed(eventSource) {
        const closedState = typeof EventSource !== 'undefined' && EventSource.CLOSED !== undefined ? EventSource.CLOSED : 2;
        return eventSource.readyState === closedState;
    }

    isOpen(eventSource) {
        const openState = typeof EventSource !== 'undefined' && EventSource.OPEN !== undefined ? EventSource.OPEN : 1;
        return eventSource && eventSource.readyState === openState;
    }

    reconnect() {
        if (this.endpointUrls.length > 0) {
            this.connectAll(this.endpointUrls);
        }
    }

    scheduleReconnect(endpointUrl) {
        if (this.browserNavigationInProgress || this.appBrowserNavigationInProgress()) {
            return;
        }
        if (this.reconnectTimers[endpointUrl]) {
            return;
        }
        const attempt = this.reconnectAttempts[endpointUrl] || 0;
        const maxDelay = this.pendingEventTtlMs > 0
                ? Math.min(this.maxReconnectDelayMs, this.pendingEventTtlMs)
                : this.maxReconnectDelayMs;
        const delay = attempt === 0 ? 0 : Math.min(this.reconnectDelayMs * Math.pow(2, attempt - 1), maxDelay);
        this.reconnectAttempts[endpointUrl] = attempt + 1;
        this.reconnectTimers[endpointUrl] = setTimeout(() => {
            delete this.reconnectTimers[endpointUrl];
            if (this.endpointUrls.includes(endpointUrl)) {
                this.connectEndpoint(endpointUrl);
            }
        }, delay);
    }

    setPendingEventTtlMs(pendingEventTtlMs) {
        this.pendingEventTtlMs = pendingEventTtlMs || 0;
    }

    requestAuthenticationCheck(endpointUrl) {
        if (this.browserNavigationInProgress || this.appBrowserNavigationInProgress()) {
            return;
        }
        if (typeof app === 'undefined' || typeof app.checkAuthenticationAfterSseError !== 'function') {
            return;
        }
        app.checkAuthenticationAfterSseError(endpointUrl);
    }

    shouldIgnoreConnectionError(endpointUrl, eventSource) {
        return this.browserNavigationInProgress
            || this.appBrowserNavigationInProgress()
            || this.eventSources[endpointUrl] !== eventSource;
    }

    appBrowserNavigationInProgress() {
        return typeof app !== 'undefined'
            && typeof app.isBrowserNavigationInProgress === 'function'
            && app.isBrowserNavigationInProgress();
    }

    close() {
        for (const endpointUrl of Object.keys(this.eventSources)) {
            this.closeEndpoint(endpointUrl);
        }
        this.eventSource = null;
        this.connected = false;
    }

    closeForBrowserNavigation() {
        this.browserNavigationInProgress = true;
        this.close();
    }

    closeEndpoint(endpointUrl, resetAttempts = true) {
        const eventSource = this.eventSources[endpointUrl];
        if (eventSource) {
            eventSource.close();
            delete this.eventSources[endpointUrl];
        }
        delete this.connectionPromises[endpointUrl];
        if (this.reconnectTimers[endpointUrl]) {
            clearTimeout(this.reconnectTimers[endpointUrl]);
            delete this.reconnectTimers[endpointUrl];
        }
        if (resetAttempts) {
            delete this.reconnectAttempts[endpointUrl];
        }
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
