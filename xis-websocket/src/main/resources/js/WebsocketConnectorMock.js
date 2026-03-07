/**
 * WebSocket mock for integration tests.
 * Replaces the real WebsocketConnector when xis-websocket is on the test classpath.
 *
 * send()              → delegates synchronously to backendBridge, same as HttpConnectorMock
 * simulatePushEvent() → injects a server-push update-event directly into the running app,
 *                       exactly as WebsocketConnector.handlePushMessage() would do
 * connect()           → no-op, always succeeds immediately
 */
class WebsocketConnectorMock {

    constructor(clientId) {
        this.clientId = clientId;
        this.messageId = 0;
        this.connected = true;
    }

    /**
     * No-op – in tests there is no real WebSocket connection needed.
     * @public
     * @returns {Promise<void>}
     */
    connect(url) {
        return Promise.resolve();
    }

    /**
     * Delegates the WebSocket message to backendBridge (same transport as HttpConnectorMock).
     * @public
     * @param {string} path
     * @param {string} method
     * @param {any} body
     * @param {object} headers
     * @returns {Promise<any>}
     */
    send(path, method, body, headers = {}) {
        return new Promise((resolve, reject) => {
            try {
                const response = backendBridge.invokeBackend(
                    method,
                    path,
                    headers || {},
                    JSON.stringify(body || {})
                );
                resolve(response);
            } catch (e) {
                reportError("WebsocketConnectorMock: error during backend invocation: " + e);
                reject(e);
            }
        });
    }

    /**
     * Simulates a server-push update-event arriving on the WebSocket.
     * Call this from Java via IntegrationTestContext.simulatePushEvent(key).
     *
     * @param {string} updateEventKey
     * @returns {Promise<void>}
     */
    simulatePushEvent(updateEventKey) {
        return app.pageController.handleUpdateEvents([updateEventKey])
            .then(pageUpdated => {
                if (!pageUpdated) {
                    return app.widgetContainers.handleUpdateEvents([updateEventKey]);
                }
            });
    }

    isConnected() {
        return this.connected;
    }

    close() {
        // no-op in tests
    }
}
