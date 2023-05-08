class CompositeTagHandler {

    constructor(element) {
        this.element = element;
        this.handlers = [];
    }

    addHandler(handler) {
        this.handlers.push(handler);
    }

    refresh(data) {
        this.handlers.forEach(handler => handler.refresh(data));
    }
}