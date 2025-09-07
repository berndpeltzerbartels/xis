class EventPublisher {
    constructor() {
        this.listeners = {};
    }

    addEventListener(event, listener) {
        if (!this.listeners[event]) {
            this.listeners[event] = [];
        }
        this.listeners[event].push(listener);
    }

    removeEventListener(event, listener) {
        if (!this.listeners[event]) return;
        const index = this.listeners[event].indexOf(listener);
        if (index > -1) {
            this.listeners[event].splice(index, 1);
        }
    }

    publish(event, data) {
        if (!this.listeners[event]) return;
        this.listeners[event].forEach(listener => listener(data));
    }
}