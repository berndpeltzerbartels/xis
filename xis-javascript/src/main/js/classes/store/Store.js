class Store {
    constructor() {
        this.listeners = {};
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
       throw new Error('getValue must be implemented in the subclass');
    }

    saveValue(path, value) {
        throw new Error('saveValue must be implemented in the subclass');
    }


    /**
     * 
     * @param {{string: any}} values 
     */
    saveData(values) {
        for (var path of Object.keys(values)) {
            this.saveValue(path, values[path]);
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