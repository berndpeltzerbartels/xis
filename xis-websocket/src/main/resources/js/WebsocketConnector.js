class WebsocketConnector {

    constructor(clientId) {
        this.ws = null;
        this.connected = false;
        this.pendingRequests = new Map();
        this.messageId = 0;
        this.url = null;
        this.clientId = clientId;
        /** @private */
        this.queue = [];
        /** @private */
        this.processing = false;
        /** @private */
        this.pingInterval = null;
        /** @private */
        this.pongTimeout = null;
        /** @private */
        this.reconnecting = false;
    }

    /**
     * Connect to WebSocket server.
     * Returns a Promise that resolves when the connection is established,
     * or rejects after all reconnect attempts have failed.
     * @public
     * @param {string} url - WebSocket URL (e.g., ws://localhost:8080/ws)
     * @returns {Promise<void>}
     */
    connect(url) {
        this.url = url;
        return this.doConnect().then(() => this.sendConnectMessage());
    }


    /**
     * Send message via WebSocket
     * @public
     * @param {string} path
     * @param {string} method
     * @param {any} body
     * @param {object} headers
     * @returns {Promise<any>}
     */
    send(path, method, body, headers = {}) {
        return new Promise((resolve, reject) => {
            this.queue.push({ path, method, body, headers, resolve, reject });
            this.processQueue();
        });
    }

    /**
     * @private
     */
    processQueue() {
        if (this.processing || this.queue.length === 0) {
            return;
        }
        this.processing = true;
        const { path, method, body, headers, resolve, reject } = this.queue.shift();

        const finish = (fn, arg) => {
            this.processing = false;
            fn(arg);
            this.processQueue();
        };

        const doSend = () => new Promise((res, rej) => {
            this.doSend(path, method, body, headers, res, rej);
        });

        if (this.isConnected()) {
            doSend().then(r => finish(resolve, r)).catch(e => finish(reject, e));
        } else {
            new Promise((res, rej) => this.scheduleReconnect(res, rej))
                .then(() => doSend())
                .then(r => finish(resolve, r))
                .catch(e => finish(reject, e));
        }
    }


    /**
     * @private
     * @returns {Promise<void>}
     */
    doConnect() {
        return new Promise((resolve, reject) => {
            try {
                // Close any stale connection before opening a new one.
                // stopPing() must be called explicitly here because we null out
                // the onclose handler below, so it will never fire for the old socket.
                if (this.ws) {
                    this.stopPing();
                    this.ws.onopen = null;
                    this.ws.onmessage = null;
                    this.ws.onerror = null;
                    this.ws.onclose = null;
                    this.ws.close();
                    this.ws = null;
                }
                this.connected = false;

                this.ws = new WebSocket(this.url);

                this.ws.onopen = () => {
                    console.debug('websocket connected to ' + this.url);
                    this.connected = true;
                    this.startPing();

                    // Switch onclose to the running-state handler
                    this.ws.onclose = (event) => {
                        console.debug('websocket closed:', event.code, event.reason);
                        this.connected = false;
                        this.ws = null;
                        this.stopPing();
                        this.reconnect();
                    };

                    resolve();
                };

                this.ws.onerror = (error) => {
                    console.error('WebSocket error:', error);
                    // onclose will fire after onerror
                };

                this.ws.onmessage = (event) => {
                    console.debug("message received");
                    this.handleMessage(event.data);
                };

                // onclose before onopen = connection failed, reject so caller can retry
                this.ws.onclose = (event) => {
                    console.debug('websocket closed before open:', event.code, event.reason);
                    this.connected = false;
                    this.ws = null;
                    reject(new Error('WebSocket closed before connection was established'));
                };

            } catch (e) {
                reportError('Error creating WebSocket connection to ' + this.url, e);
                reject(e);
            }
        });
    }

    /**
     * Tries to establish a connection up to 5 times with 1-second intervals.
     * Resolves when connected, rejects when all attempts are exhausted.
     * Used by processQueue and sendPushAck to wait for a connection before sending.
     * @private
     * @param {Function} resolve
     * @param {Function} reject
     * @param {number} [attempt=1]
     */
    scheduleReconnect(resolve, reject, attempt) {
        attempt = attempt || 1;
        const maxAttempts = 5;

        if (attempt > maxAttempts) {
            console.error('[WS] scheduleReconnect: gave up after ' + maxAttempts + ' attempts');
            reject(new Error('WebSocket reconnect failed after ' + maxAttempts + ' attempts'));
            return;
        }

        console.debug('[WS] scheduleReconnect: attempt ' + attempt + '/' + maxAttempts + ' in 1s');

        setTimeout(() => {
            this.doConnect()
                .then(() => {
                    this.sendReconnectMessage();
                    resolve();
                })
                .catch(() => this.scheduleReconnect(resolve, reject, attempt + 1));
        }, 1000);
    }

    /**
     * @private
     */
    sendConnectMessage() {
        try {
            this.ws.send(JSON.stringify({
                'request-type': 'connect',
                clientId: this.clientId,
                messageId: 0
            }));
            console.debug('[WS] sent CONNECT message clientId=' + this.clientId);
        } catch (e) {
            reportError('[WS] failed to send CONNECT message', e);
        }
    }

    /**
     * @private
     */
    sendReconnectMessage() {
        try {
            this.ws.send(JSON.stringify({
                'request-type': 'reconnect',
                clientId: this.clientId,
                messageId: 0
            }));
            console.debug('[WS] sent RECONNECT message clientId=' + this.clientId);
        } catch (e) {
            reportError('[WS] failed to send RECONNECT message', e);
        }
    }

    /**
     * Restarts the connection after an established connection was lost.
     * Retries up to 5 times with 1-second intervals.
     * If all attempts fail, reports an error and rejects all queued requests.
     * @private
     * @param {number} [attempt=1]
     */
    reconnect(attempt) {
        if (this.reconnecting) {
            return;
        }
        this.reconnecting = true;
        this.doReconnect(attempt || 1);
    }

    /**
     * @private
     */
    doReconnect(attempt) {
        const maxAttempts = 5;

        if (attempt > maxAttempts) {
            console.error('[WS] reconnect: gave up after ' + maxAttempts + ' attempts');
            this.reconnecting = false;
            app.messageHandler.reportServerError('connection lost');
            this.rejectQueue(new Error('WebSocket connection lost after ' + maxAttempts + ' reconnect attempts'));
            return;
        }

        console.debug('[WS] reconnect: attempt ' + attempt + '/' + maxAttempts + ' in 1s');

        setTimeout(() => {
            this.doConnect()
                .then(() => {
                    console.debug('[WS] reconnect: success on attempt ' + attempt);
                    this.sendReconnectMessage();
                    this.reconnecting = false;
                })
                .catch(() => this.doReconnect(attempt + 1));
        }, 1000);
    }

    /**
     * Rejects all queued and pending requests with the given error.
     * @private
     * @param {Error} error
     */
    rejectQueue(error) {
        this.processing = false;
        this.queue.forEach(entry => entry.reject(error));
        this.queue = [];
        this.pendingRequests.forEach(pending => pending.reject(error));
        this.pendingRequests.clear();
    }

    /**
     * Check if WebSocket is connected
     * @public
     * @returns {boolean}
     */
    isConnected() {
        return this.connected && this.ws !== null && this.ws.readyState === WebSocket.OPEN;
    }
    /**
     * @private
     * @param {string} data
     */
    handleMessage(data) {
        try {
            const obj = JSON.parse(data);
            switch (obj.messageType) {
                case 'PONG':
                    console.debug('[WS] pong received');
                    this.handlePong();
                    break;
                case 'PUSH':
                    console.debug('[WS] push message received');
                    this.handlePushMessage(new WebsocketPushMessage(obj));
                    break;
                default:
                    console.debug('[WS] server response received');
                    this.handleServerResponse(new WebsocketServerResponse(data));
            }
        } catch (e) {
            reportError('Error parsing WebSocket message', e);
        }
    }

    /**
     * @private
     */
    handlePong() {
        console.debug('[WS] pong received');
        clearTimeout(this.pongTimeout);
        this.pongTimeout = null;
    }

    /**
     * @private
     * @param {WebsocketServerResponse} response
     */
    handleServerResponse(response) {
        console.debug('[WS] handleServerResponse: status=' + response.status
            + ' messageId=' + response.messageId
            + ' pendingCount=' + this.pendingRequests.size);


        const messageId = response.messageId;
        if (!messageId) {
            reportError('[WS] received server response without messageId', null);
            return;
        }

        if (this.pendingRequests.has(messageId)) {
            console.debug('[WS] resolving pending request messageId=' + messageId);
            const pending = this.pendingRequests.get(messageId);
            this.pendingRequests.delete(messageId);
            pending.resolve(response);
        } else {
            console.warn('[WS] no pending request found for messageId=' + messageId);
        }
    }

    /**
     * @private
     * @param {WebsocketPushMessage} message
     */
    handlePushMessage(message) {
        console.debug('[WS] push update-event: key=' + message.updateEventKey);
        this.sendPushAck(message.eventId);
        app.pageController.handleUpdateEvents([message.updateEventKey])
            .then(pageUpdated => {
                if (!pageUpdated) {
                    app.widgetContainers.handleUpdateEvents([message.updateEventKey]);
                }
            })
            .catch(e => reportError('[WS] error handling push update-event key=' + message.updateEventKey, e));
    }

    /**
     * @private
     * @param {string} eventId
     */
    sendPushAck(eventId) {
        const doAck = () => {
            try {
                this.ws.send(JSON.stringify({
                    'request-type': 'push-ack',
                    clientId: this.clientId,
                    eventId: eventId
                }));
                console.debug('[WS] push ACK sent for eventId=' + eventId);
            } catch (e) {
                reportError('[WS] failed to send push ACK for eventId=' + eventId, e);
            }
        };

        if (this.isConnected()) {
            doAck();
        } else {
            new Promise((res, rej) => this.scheduleReconnect(res, rej))
                .then(() => doAck())
                .catch(e => reportError('[WS] failed to send push ACK after reconnect for eventId=' + eventId, e));
        }
    }


    /**
     * @private
     */
    startPing() {
        this.pingInterval = setInterval(() => this.sendPing(), 20000);
    }

    /**
     * @private
     */
    stopPing() {
        clearInterval(this.pingInterval);
        this.pingInterval = null;
        clearTimeout(this.pongTimeout);
        this.pongTimeout = null;
    }

    /**
     * @private
     */
    sendPing() {
        if (!this.isConnected()) {
            return;
        }
        console.debug('[WS] sending ping');
        this.ws.send(JSON.stringify({ 'request-type': 'ping', clientId: this.clientId }));
        this.pongTimeout = setTimeout(() => {
            console.warn('[WS] pong timeout – connection dead, reconnecting');
            this.stopPing();
            this.connected = false;
            this.ws.close();
            this.ws = null;
            this.reconnect();
        }, 5000);
    }

    /**
     * @private
     */
    doSend(path, method, body, headers, resolve, reject) {
        try {
            var messageId = ++this.messageId;
            console.debug('[WS] doSend: path=' + path + ' messageId=' + messageId);

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
        this.stopPing();
        this.reconnecting = true; // prevent reconnect() from firing after ws.close()

        if (this.ws) {
            this.ws.close();
            this.ws = null;
            this.connected = false;
        }

        this.rejectQueue(new Error('WebSocket closed by client'));
    }


}

// Constants (ES5-compatible)
WebsocketConnector.INITIAL_RECONNECT_DELAY = 1000; // 1 second
WebsocketConnector.MAX_RECONNECT_DELAY = 30000; // 30 seconds
WebsocketConnector.RECONNECT_BACKOFF_MULTIPLIER = 2;
WebsocketConnector.REQUEST_TIMEOUT = 30000;
