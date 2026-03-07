class WebsocketServerResponse {

    constructor(string) {
        const obj = JSON.parse(string);
        this.responseType = obj.responseType || null;
        this.status = parseInt(obj.status);
        this.body = obj.body;
        this.headers = obj.headers;
        this.messageId = obj.messageId;
        // Push-only fields
        this.updateEventKey = obj.updateEventKey || null;
    }

    getResponseHeader(name) {
        return this.headers[name];
    }

    isPushMessage() {
        return this.responseType != null;
    }

}