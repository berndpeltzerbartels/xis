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

            try {
                this.ws = new WebSocket(this.url);

                this.ws.onopen = () => {
                    console.debug('websocket connected to ' + this.url);
                    this.connected = true;
                    if (this.reconnectAttempts > 0) {
                        console.log('reconnected successfully after ' + this.reconnectAttempts + ' attempts');
                        this.reconnectAttempts = 0;
                        this.sendReconnectMessage();
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
                    this.pendingRequests.forEach((pending, messageId) => {
                        console.debug("removinf pending request");
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
            console.log('Sent RECONNECT message with clientId: ' + this.clientId);
        } catch (e) {
            console.error('Failed to send RECONNECT message:', e);
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
            const messageId = response.messageId;
            
            if (!messageId) {
                throw new Error("no message id");
            }

            if (this.pendingRequests.has(messageId)) {
                const pending = this.pendingRequests.get(messageId);
                this.pendingRequests.delete(messageId);
                pending.resolve(response);
            }
        } catch (e) {
            reportError('Error parsing WebSocket message', e);
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
            if (!self.connected || !self.ws) {
                // Wait for reconnect and retry
                var retryAttempts = 0;
                var maxRetries = 50; // 5 seconds total (50 * 100ms)
                var retryInterval = setInterval(function() {
                    retryAttempts++;
                    if (self.connected && self.ws) {
                        clearInterval(retryInterval);
                        self.doSend(path, method, body, headers, resolve, reject);
                    } else if (retryAttempts >= maxRetries) {
                        clearInterval(retryInterval);
                        reject(new Error('WebSocket not connected'));
                    }
                }, 100);
                return;
            }

            self.doSend(path, method, body, headers, resolve, reject);
        });
    }

    /**
     * Internal method to actually send the message
     * @private
     */
    doSend(path, method, body, headers, resolve, reject) {
        try {
            var messageId = ++this.messageId;

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
                    self.pendingRequests.delete(messageId);
                    reject(new Error('WebSocket request timeout'));
                }
            }, 30000); // 30 second timeout

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
