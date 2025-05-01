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
        var key = '';
        for (var pathElement of path.split('.')) {
            if (key !== '') key += '.';
            key += pathElement;
            if (this.listeners[key] === undefined) {
                this.listeners[key] = [];
            }
            this.listeners[key].push(listener);
        }
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
            // check if value is a function or method
            if (typeof value === 'function') {
                continue; // For debugging with Java-objects, to skip toString etc
            }
            if (this.listeners[path] !== undefined) {
               for (var listener of  this.listeners[path]) {
                    listener();
                };
            }
        }
    }
}