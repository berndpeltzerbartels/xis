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

    getValue(path) {
        return this.data.getValueByPath(path);
    }


    /**
     * 
     * @param {{string: any}} values 
     */
    publish(values) {
        for (var path of Object.keys(values)) {
            this.data.setValueByPath(path, values[path]);
            var value = values[path];
            if (this.listeners[path] !== undefined) {
                this.listeners[path].forEach(listener => {
                    listener(value);
                });
            }
        }
    }
}