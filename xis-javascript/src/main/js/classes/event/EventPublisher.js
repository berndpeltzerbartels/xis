class EventPublisher {
    constructor() {
        this.listeners = {};
    }

    addEventListener(eventKey, listener) {
        console.log(`Adding listener for event: ${eventKey}`);
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
                console.log(`Removed listener from event: ${eventKey}`);
            }
       }
    }

    publish(eventKey, data) {
        console.log(`Publishing event: ${eventKey}`, data);
        if (!this.listeners[eventKey]) return;
        this.listeners[eventKey].forEach(listener => listener(data));
    }
}