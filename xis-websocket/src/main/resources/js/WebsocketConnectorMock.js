class WebsocketConnectorMock {

    constructor(clientId) {
        this.ws = null;
        this.messageId = 0;
        this.connected = false;
        this.clientId = clientId;
    }

    /**
     * Connect to WebSocket (mock via backendBridge)
     * @public
     * @param {string} url 
     * @returns {Promise<void>}
     */
    connect(url) {
        return new Promise((resolve, reject) => {
            try {
                console.log('WSConnectorMock: Connecting to ' + url);
                
                // Initialize mock WebSocket connection via backendBridge
                const response = backendBridge.invokeBackend(
                    'WS_CONNECT',
                    url,
                    {},
                    null
                );
                
                if (response.status === 'connected') {
                    this.ws = { connected: true, url: url };
                    this.connected = true;
                    console.log('WSConnectorMock: Connected');
                    resolve();
                } else {
                    const error = new Error('WebSocket connection failed');
                    reject(error);
                }
            } catch (e) {
                reportError("Error during WebSocket connection: " + e);
                reject(e);
            }
        });
    }

    /**
     * Send WebSocket message
     * @public
     * @param {string} url - Full URL with path and query
     * @param {string} method - HTTP method (GET, POST, etc.)
     * @param {any} body - Request body
     * @param {object} headers - Request headers
     * @returns {Promise<any>}
     */
    send(url, method, body, headers = {}) {
        return new Promise((resolve, reject) => {
            if (!this.ws || !this.ws.connected) {
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
                    'request-type': 'client-request',
                    clientId: this.clientId,
                    messageId: messageId,
                    path: path,
                    method: method,
                    queryParameters: queryParameters,
                    headers: headers,
                    body: body
                };

                this.logRequest('WS', url, message, headers);

                // Send via backendBridge
                const response = backendBridge.invokeBackend(
                    'WS_SEND',
                    url,
                    headers,
                    JSON.stringify(message)
                );

                resolve(response);

            } catch (e) {
                reportError("Error during WebSocket send: " + e);
                reject(e);
            }
        });
    }

    /**
     * Close WebSocket connection
     * @public
     */
    close() {
        if (this.ws && this.ws.connected) {
            console.log('WSConnectorMock: Closing connection');
            this.ws.connected = false;
            this.connected = false;
            this.ws = null;
        }
    }

    /**
     * Check if WebSocket is connected
     * @public
     * @returns {boolean}
     */
    isConnected() {
        return this.connected && this.ws && this.ws.connected;
    }

    logRequest(protocol, uri, payload, headers) {
        console.log('protocol: ' + protocol);
        console.log('uri: ' + uri);
        if (headers) {
            for (const key in headers) {
                console.log('header: ' + key + ' : ' + headers[key]);
            }
        }
    }
}
