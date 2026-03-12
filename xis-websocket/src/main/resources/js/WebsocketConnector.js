class WebsocketConnector {

    constructor(clientId) {
        this.ws = null;
        this.connected = false;
        this.url = null;
        this.clientId = clientId;
        /** @private */
        this.pingInterval = null;
        /** @private */
        this.pongTimeout = null;
        /** @private */
        this.reconnecting = false;
        /** @private Timestamp (ms) when the connection was lost – used to detect missed push events. */
        this.disconnectedAt = null;
        /** @private TTL in ms – set from server config via setPendingEventTtlMs(). */
        this.pendingEventTtlMs = WebsocketConnector.PENDING_EVENT_TTL_MS;
    }

    /**
     * Sets the pending-event TTL from the server config.
     * @public
     * @param {number} ms
     */
    setPendingEventTtlMs(ms) {
        if (ms > 0) {
            this.pendingEventTtlMs = ms;
        }
    }

    /**
     * Connect to WebSocket server.
     * @public
     * @param {string} url
     * @returns {Promise<void>}
     */
    connect(url) {
        this.url = url;
        return this.doConnect().then(() => this.sendConnectMessage());
    }

    /**
     * @private
     * @returns {Promise<void>}
     */
    doConnect() {
        return new Promise((resolve, reject) => {
            try {
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
                    console.debug('[WS] connected to ' + this.url);
                    this.connected = true;
                    this.startPing();
                    this.ws.onclose = (event) => {
                        console.debug('[WS] closed:', event.code, event.reason);
                        this.connected = false;
                        this.ws = null;
                        this.stopPing();
                        this.disconnectedAt = Date.now();
                        this.reconnect();
                    };
                    resolve();
                };

                this.ws.onerror = (error) => {
                    console.error('[WS] error:', error);
                };

                this.ws.onmessage = (event) => {
                    this.handleMessage(event.data);
                };

                this.ws.onclose = (event) => {
                    console.debug('[WS] closed before open:', event.code, event.reason);
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
     * @private
     */
    sendConnectMessage() {
        try {
            this.ws.send(JSON.stringify({ 'request-type': 'connect', clientId: this.clientId }));
            console.debug('[WS] sent CONNECT clientId=' + this.clientId);
        } catch (e) {
            reportError('[WS] failed to send CONNECT message', e);
        }
    }

    /**
     * @private
     */
    sendReconnectMessage() {
        try {
            this.ws.send(JSON.stringify({ 'request-type': 'reconnect', clientId: this.clientId }));
            console.debug('[WS] sent RECONNECT clientId=' + this.clientId);
        } catch (e) {
            reportError('[WS] failed to send RECONNECT message', e);
        }
    }

    /**
     * @private
     */
    reconnect() {
        if (this.reconnecting) {
            return;
        }
        this.reconnecting = true;
        this.doReconnect(1);
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
            return;
        }
        console.debug('[WS] reconnect: attempt ' + attempt + '/' + maxAttempts + ' in 1s');
        setTimeout(() => {
            this.doConnect()
                .then(() => {
                    console.debug('[WS] reconnect: success on attempt ' + attempt);
                    this.sendReconnectMessage();
                    this.reconnecting = false;
                    this.onReconnected();
                })
                .catch(() => this.doReconnect(attempt + 1));
        }, 1000);
    }

    /**
     * Called after a successful reconnect.
     * If offline longer than TTL → page reload so user sees consistent state.
     * @private
     */
    onReconnected() {
        if (this.disconnectedAt !== null) {
            const downMs = Date.now() - this.disconnectedAt;
            this.disconnectedAt = null;
            if (downMs > this.pendingEventTtlMs) {
                console.info('[WS] onReconnected: offline for ' + downMs + 'ms – refreshing page');
                app.pageController.refreshCurrentPage()
                    .catch(e => reportError('[WS] error refreshing page after long disconnect', e));
            }
        }
    }

    /**
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
                    this.handlePong();
                    break;
                case 'PUSH':
                    console.debug('[WS] push received');
                    this.handlePushMessage(new WebsocketPushMessage(obj));
                    break;
                default:
                    reportError('[WS] unknown message type: ' + obj.messageType, null);
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
        if (this.isConnected()) {
            this.doSendAck(eventId);
        } else {
            this.doReconnectThen(() => this.doSendAck(eventId),
                e => reportError('[WS] failed to send push ACK after reconnect for eventId=' + eventId, e));
        }
    }

    /**
     * @private
     */
    doSendAck(eventId) {
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
    }

    /**
     * Tries to reconnect once (up to 5 attempts) and then calls onSuccess or onError.
     * Used for fire-and-forget operations like sending an ACK.
     * @private
     */
    doReconnectThen(onSuccess, onError) {
        new Promise((res, rej) => {
            let attempt = 1;
            const maxAttempts = 5;
            const tryConnect = () => {
                if (attempt > maxAttempts) { rej(new Error('reconnect failed')); return; }
                setTimeout(() => {
                    this.doConnect()
                        .then(() => { this.sendReconnectMessage(); res(); })
                        .catch(() => { attempt++; tryConnect(); });
                }, 1000);
            };
            tryConnect();
        }).then(onSuccess).catch(onError);
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
            console.warn('[WS] pong timeout – reconnecting');
            this.stopPing();
            this.connected = false;
            this.ws.close();
            this.ws = null;
            this.reconnect();
        }, 5000);
    }

    /**
     * Close WebSocket connection.
     * @public
     */
    close() {
        this.stopPing();
        this.reconnecting = true;
        if (this.ws) {
            this.ws.close();
            this.ws = null;
            this.connected = false;
        }
    }
}

WebsocketConnector.PENDING_EVENT_TTL_MS = 30 * 60 * 1000; // 30 minutes – must match WSService.PENDING_EVENT_TTL
