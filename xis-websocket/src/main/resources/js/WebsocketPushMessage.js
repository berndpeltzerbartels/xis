class WebsocketPushMessage {

    /**
     * @param {object} obj - parsed JSON object from the server
     */
    constructor(obj) {
        this.eventId = obj.eventId;
        this.updateEventKey = obj.updateEventKey;
    }

}
