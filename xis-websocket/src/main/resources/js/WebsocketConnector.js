class WebsocketConnector {

    constructor() {
        this.ws = null;
        this.connected = false;
        this.pendingRequests = new Map();
        this.messageId = 0;
    }

    /**
     * Connect to WebSocket server
     * @public
     * @param {string} url - WebSocket URL (e.g., ws://localhost:8080/ws)
     * @returns {Promise<void>}
     */
    connect(url) {
        return new Promise((resolve, reject) => {
            if (this.connected) {
                resolve();
                return;
            }

            try {
                this.ws = new WebSocket(url);

                this.ws.onopen = () => {
                    console.log('WebSocket connected to ' + url);
                    this.connected = true;
                    resolve();
                };

                this.ws.onerror = (error) => {
                    console.error('WebSocket error:', error);
                    reject(error);
                };

                this.ws.onmessage = (event) => {
                    this.handleMessage(event.data);
                };

                this.ws.onclose = (event) => {
                    console.log('WebSocket closed:', event.code, event.reason);
                    this.connected = false;
                    this.ws = null;
                };

            } catch (e) {
                reportError('Error creating WebSocket connection to ' + url, e);
                reject(e);
            }
        });
    }

    /**
     * Handle incoming WebSocket message
     * @private
     * @param {string} data
     */
    handleMessage(data) {
        try {
            const response = new HttpLikeResponse(data);
            const messageId = response.messageId;
            if (!messageId) {
                throw new Error("no message id");
            }

            if (messageId && this.pendingRequests.has(messageId)) {
                const pending = this.pendingRequests.get(messageId);
                this.pendingRequests.delete(messageId);
                pending.resolve(response);
            } else {
                console.warn('Received message without matching request:', response);
            }
        } catch (e) {
            reportError('Error parsing WebSocket message', e);
        }
    }

    /**
     * Send message via WebSocket
     * @public
     * @param {string} url - Full URL with path and query
     * @param {string} method - HTTP method (GET, POST, etc.)
     * @param {any} body - Request body
     * @param {object} headers - Request headers
     * @returns {Promise<any>}
     */
    send(url, method, body, headers = {}) {
        return new Promise((resolve, reject) => {
            if (!this.connected || !this.ws) {
                reject(new Error('WebSocket not connected'));
                return;
            }

            try {
                const messageId = ++this.messageId;

                // Extract path and query parameters
                const urlParts = url.split('?');
                const path = urlParts[0];
                const queryParameters = {};

                if (urlParts[1]) {
                    const params = urlParts[1].split('&');
                    for (const param of params) {
                        const [key, value] = param.split('=');
                        queryParameters[decodeURIComponent(key)] = decodeURIComponent(value || '');
                    }
                }

                const message = {
                    messageId: messageId,
                    path: path,
                    method: method,
                    queryParameters: queryParameters,
                    headers: headers,
                    body: body
                };

                this.pendingRequests.set(messageId, { resolve, reject });

                // Set timeout for request
                setTimeout(() => {
                    if (this.pendingRequests.has(messageId)) {
                        this.pendingRequests.delete(messageId);
                        reject(new Error('WebSocket request timeout'));
                    }
                }, 30000); // 30 second timeout

                this.ws.send(JSON.stringify(message));

            } catch (e) {
                reportError('Error sending WebSocket message', e);
                reject(e);
            }
        });
    }

    /**
     * Close WebSocket connection
     * @public
     */
    close() {
        if (this.ws) {
            this.ws.close();
            this.ws = null;
            this.connected = false;
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