class TagHandlers {

    // TODO ein Tag kann mehr als einen hanlder haben
    constructor() {
        this.rootHandlers = new Map();
        this.handlers  = new Map();
    }

    mapHandler(element, handler) {
        this.handlers.set(element, handler);
    }
    getHandler(element) {
        return this.handlers.get(element);
    }

    mapRootHandler(element, handler) {
        this.rootHandlers.set(element, handler);
    }

    getRootHandler(element) {
        return this.rootHandlers.get(element);
    }

}