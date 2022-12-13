class XISActions {

    constructor() {
        this.listeners = new XISMap();
    }

    addListener(action, callback) {
        this.listeners.putIfAbsent(action, []).push(callback);
    }

    /**
     * TODO bint to events
     * @param {String} action 
     */
    publishAction(action) {
        if (this.listeners.containsKey(action)) {
            for (var listener of this.listeners) {
                listener(action);
            }
        }
    }
}