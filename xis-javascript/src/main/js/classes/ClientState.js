class ClientState {
    constructor() {
        this.variables = {};
        this.listeners = {};
    }

    registerListener(path, listener) {
        if (this.variables[path] === undefined) {
            this.variables[path] = [];
        }
        this.variables[path].push(listener);
    }


    publish(path, value) {
        var pathElements = path.split('.');
        var path = '';
        for (var i = 0; i < pathElements.length; i++) {
            if (i > 0) {
                path += '.';
            }
            path += pathElements[i];
            if (this.listeners[path] !== undefined) {
                this.listeners[path].forEach(listener => {
                    listener(value);
                });
            }
        }
    }
}