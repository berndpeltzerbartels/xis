class WebsocketConnector {

    constructor(clientId) {
        this.ws = null;
        this.connected = false;
        this.pendingRequests = new Map();
        this.messageId = 0;
        this.url = null;
        this.clientId = clientId;
        this.shouldReconnect = true;
        this.reconnectAttempts = 0;
        this.reconnectTimeout = null;
        /**
         * Resolved when the connection is fully ready to accept application messages.
         * During reconnect, this stays unresolved until sendReconnectMessage() has been sent,
         * so that queued sends don't race ahead of the reconnect handshake.
         * @private
         */
        this._readyPromise = Promise.resolve();
        this._readyResolve = null;
    }

    /**
     * Connect to WebSocket server
     * @public
     * @param {string} url - WebSocket URL (e.g., ws://localhost:8080/ws)
     * @returns {Promise<void>}
     */
    connect(url) {
        this.url = url;
        this.shouldReconnect = true;
        this.reconnectAttempts = 0;
        return this.doConnect();
    }

    /**
     * Internal connect implementation
     * @private
     * @returns {Promise<void>}
     */
    doConnect() {
        return new Promise((resolve, reject) => {
            if (this.connected) {
                console.debug('websocket already connected');
                resolve();
                return;
            }

            // Fresh reconnect: hold sends until the reconnect handshake is done
            if (this.reconnectAttempts > 0) {
                this._readyPromise = new Promise(res => { this._readyResolve = res; });
            } else {
                this._readyPromise = Promise.resolve();
                this._readyResolve = null;
            }

            try {
                this.ws = new WebSocket(this.url);

                this.ws.onopen = () => {
                    console.debug('websocket connected to ' + this.url);
                    this.connected = true;
                    if (this.reconnectAttempts > 0) {
                        console.log('reconnected successfully after ' + this.reconnectAttempts + ' attempts');
                        this.reconnectAttempts = 0;
                        this.sendReconnectMessage();
                        // Unblock queued sends only after the reconnect message is on the wire
                        if (this._readyResolve) {
                            this._readyResolve();
                            this._readyResolve = null;
                        }
                    }
                    resolve();
                };

                this.ws.onerror = (error) => {
                    console.error('WebSocket error:', error);
                    reject(error);
                };

                this.ws.onmessage = (event) => {
                    console.debug("message received");
                    this.handleMessage(event.data);
                };

                this.ws.onclose = (event) => {
                    console.debug('websocket closed:', event.code, event.reason);
                    this.connected = false;
                    this.ws = null;

                    // Reject all pending requests
                    this.pendingRequests.forEach((pending) => {
                        pending.reject(new Error('websocket connection closed'));
                    });
                    this.pendingRequests.clear();

                    if (this.shouldReconnect) {
                        this.scheduleReconnect();
                    }
                };

            } catch (e) {
                reportError('Error creating WebSocket connection to ' + this.url, e);
                reject(e);
            }
        });
    }

    /**
     * Send RECONNECT message to server
     * @private
     */
    sendReconnectMessage() {
        try {
            var reconnectMessage = {
                'request-type': 'reconnect',
                clientId: this.clientId,
                messageId: 0
            };
            this.ws.send(JSON.stringify(reconnectMessage));
            console.log('[WS] sent RECONNECT message clientId=' + this.clientId);
        } catch (e) {
            console.error('[WS] failed to send RECONNECT message:', e);
        }
    }

    /**
     * Schedule reconnect with exponential backoff
     * @private
     */
    scheduleReconnect() {
        if (this.reconnectTimeout) {
            clearTimeout(this.reconnectTimeout);
        }

        const delay = Math.min(
            WebsocketConnector.INITIAL_RECONNECT_DELAY * Math.pow(
                WebsocketConnector.RECONNECT_BACKOFF_MULTIPLIER,
                this.reconnectAttempts
            ),
            WebsocketConnector.MAX_RECONNECT_DELAY
        );

        this.reconnectAttempts++;
        console.log('Reconnecting in ' + delay + 'ms (attempt ' + this.reconnectAttempts + ')');

        this.reconnectTimeout = setTimeout(() => {
            this.reconnectTimeout = null;
            this.doConnect().catch((error) => {
                console.error('Reconnect failed:', error);
                // Will trigger onclose which schedules next reconnect
            });
        }, delay);
    }

    /**
     * Handle incoming WebSocket message
     * @private
     * @param {string} data
     */
    handleMessage(data) {
        try {
            const response = new WebsocketServerResponse(data);
            console.debug('[WS] handleMessage: status=' + response.status
                + ' messageId=' + response.messageId
                + ' responseType=' + response.responseType
                + ' pendingCount=' + this.pendingRequests.size);

            // Server-push message (no pending request, dispatched by response-type)
            if (response.isPushMessage()) {
                console.debug('[WS] push message received: type=' + response.responseType + ' key=' + response.updateEventKey);
                this.handlePushMessage(response);
                return;
            }

            const messageId = response.messageId;

            if (!messageId) {
                throw new Error("no message id");
            }

            if (this.pendingRequests.has(messageId)) {
                console.debug('[WS] resolving pending request messageId=' + messageId);
                const pending = this.pendingRequests.get(messageId);
                this.pendingRequests.delete(messageId);
                pending.resolve(response);
            } else {
                console.warn('[WS] handleMessage: no pending request found for messageId=' + messageId);
            }
        } catch (e) {
            reportError('Error parsing WebSocket message', e);
        }
    }

    /**
     * Handle server-initiated push messages.
     * Currently supports response-type "update-event" which triggers the same
     * refresh logic as @Action(updateEventKeys=…) / @RefreshOnUpdateEvents.
     * @private
     * @param {WebsocketServerResponse} response
     */
    handlePushMessage(response) {
        switch (response.responseType) {
            case 'update-event':
                console.log('[WS] push update-event: key=' + response.updateEventKey);
                app.pageController.handleUpdateEvents([response.updateEventKey])
                    .then(pageUpdated => {
                        console.debug('[WS] push update-event: pageUpdated=' + pageUpdated + ' key=' + response.updateEventKey);
                        if (!pageUpdated) {
                            console.debug('[WS] push update-event: delegating to widgetContainers key=' + response.updateEventKey);
                            app.widgetContainers.handleUpdateEvents([response.updateEventKey]);
                        }
                    });
                break;
            default:
                console.warn('[WS] unknown push response-type:', response.responseType);
        }
    }

    /**
     * Send message via WebSocket
     * @public
     * @param {string} path, currently query parameters are not supported
     * @param {string} method - HTTP method (GET, POST, etc.)
     * @param {any} body - Request body
     * @param {object} headers - Request headers
     * @returns {Promise<any>}
     */
    send(path, method, body, headers = {}) {
        var self = this;
        return new Promise(function(resolve, reject) {
            // Wait until the connection is fully ready (incl. reconnect handshake)
            self._readyPromise.then(() => {
                if (self.isConnected()) {
                    self.doSend(path, method, body, headers, resolve, reject);
                    return;
                }

                // Not connected yet – poll until ready or timeout
                var retryAttempts = 0;
                var maxRetries = 50; // 5 seconds total (50 * 100ms)
                var retryInterval = setInterval(function() {
                    retryAttempts++;
                    if (self.isConnected()) {
                        clearInterval(retryInterval);
                        // Re-check readyPromise in case a reconnect started while we were polling
                        self._readyPromise.then(() => self.doSend(path, method, body, headers, resolve, reject));
                    } else if (retryAttempts >= maxRetries) {
                        clearInterval(retryInterval);
                        reject(new Error('WebSocket not connected'));
                    }
                }, 100);
            });
        });
    }

    /**
     * Internal method to actually send the message
     * @private
     */
    doSend(path, method, body, headers, resolve, reject) {
        try {
            if (!this.isConnected()) {
                reject(new Error('WebSocket not connected'));
                return;
            }

            var messageId = ++this.messageId;
            console.debug('[WS] doSend: path=' + path + ' messageId=' + messageId + ' pendingCount=' + this.pendingRequests.size);

            var message = {
                'request-type': 'client-request',
                clientId: this.clientId,
                messageId: messageId,
                path: path,
                method: method,
                headers: headers,
                body: body
            };

            this.pendingRequests.set(messageId, { resolve: resolve, reject: reject });

            // Set timeout for request
            var self = this;
            setTimeout(function() {
                if (self.pendingRequests.has(messageId)) {
                    console.warn('[WS] request timeout: path=' + path + ' messageId=' + messageId);
                    self.pendingRequests.delete(messageId);
                    reject(new Error('WebSocket request timeout'));
                }
            }, 30000);

            this.ws.send(JSON.stringify(message));

        } catch (e) {
            reportError('Error sending WebSocket message', e);
            reject(e);
        }
    }

    /**
     * Close WebSocket connection and stop reconnecting
     * @public
     */
    close() {
        this.shouldReconnect = false;

        if (this.reconnectTimeout) {
            clearTimeout(this.reconnectTimeout);
            this.reconnectTimeout = null;
        }

        if (this.ws) {
            this.ws.close();
            this.ws = null;
            this.connected = false;

            // Reject all pending requests
            this.pendingRequests.forEach((pending, messageId) => {
                pending.reject(new Error('WebSocket closed by client'));
            });
            this.pendingRequests.clear();
        }
    }

    /**
     * Check if WebSocket is connected
     * @public
     * @returns {boolean}
     */
    isConnected() {
        return this.connected && this.ws && this.ws.readyState === WebSocket.OPEN;
    }

}

// Constants (ES5-compatible)
WebsocketConnector.INITIAL_RECONNECT_DELAY = 1000; // 1 second
WebsocketConnector.MAX_RECONNECT_DELAY = 30000; // 30 seconds
WebsocketConnector.RECONNECT_BACKOFF_MULTIPLIER = 2;
WebsocketConnector.REQUEST_TIMEOUT = 30000;
