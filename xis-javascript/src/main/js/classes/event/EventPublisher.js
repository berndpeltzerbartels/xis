class EventPublisher {
    constructor() {
        this.listeners = {};
    }

    addEventListener(eventKey, listener) {
        if (!this.listeners[eventKey]) {
            this.listeners[eventKey] = [];
        }
        this.listeners[eventKey].push(listener);
    }

    removeEventListener(listener) {
       for (const eventKey in this.listeners) {
            const listeners = this.listeners[eventKey];
            const index = listeners.indexOf(listener);
            if (index > -1) {
                listeners.splice(index, 1);
            }
       }
    }

    publish(eventKey, data) {
        if (!this.listeners[eventKey]) return;
        this.listeners[eventKey].forEach(listener => listener(data));
    }
}