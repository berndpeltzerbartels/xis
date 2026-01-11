class ClientFactory {

    constructor(httpConnector, websocketConnector) {
        this.httpConnector = httpConnector;
        this.websocketConnector = websocketConnector;
    }


    createClient() {
        if (this.websocketConnector && this.isWebSocketSupported()) {
            return new WebsocketClient(this.websocketConnector);
        } else {
            return new HttpClient(this.httpConnector);
        }
    }

    isWebSocketSupported() {
        // Check if browser/runtime supports WebSocket
        return typeof WebSocket !== 'undefined';
    }
}