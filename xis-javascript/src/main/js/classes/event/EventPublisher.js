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

    removeEventListener(eventKey, listener) {
        if (!this.listeners[eventKey]) return;
        const index = this.listeners[eventKey].indexOf(listener);
        if (index > -1) {
            this.listeners[eventKey].splice(index, 1);
        }
    }

    publish(eventKey, data) {
        console.log(`Publishing event: ${eventKey}`, data);
        if (!this.listeners[eventKey]) return;
        this.listeners[eventKey].forEach(listener => listener(data));
    }
}