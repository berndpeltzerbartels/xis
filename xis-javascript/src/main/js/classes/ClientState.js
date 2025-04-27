class ClientState {
    constructor() {
        this.listeners = {};
        this.data = new Data({});
    }

    /**
     * 
     * @param {string} path 
     * @param {function<any>} listener {
        
     }} listener 
     */
    registerListener(path, listener) {
        if (this.listeners[path] === undefined) {
            this.listeners[path] = [];
        }
        this.listeners[path].push(listener);
    }

    /**
     * 
     * @param {{string: any}} values 
     */
    publish(values) {
        for (var path in Object.keys(values)) {
            var value = this.data.getValueByPath(path);
            this.data.setValueByPath(path, values[path]);
            if (this.listeners[path] !== undefined) {
                this.listeners[path].forEach(listener => {
                    listener(value);
                });
            }

        }
    }
}