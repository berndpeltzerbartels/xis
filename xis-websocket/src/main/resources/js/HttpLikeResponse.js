class HttpLikeResponse {

    constructor(string) {
        const obj = JSON.parse(string);
        this.status = parseInt(obj.status);
        this.body = obj.body;
        this.headers = obj.headers
        this.messageId = obj.messageId;
    }

    getResponseHeader(name) {
        return this.headers[name];
    }

}