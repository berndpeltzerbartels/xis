class ClientStore extends Store {
    constructor() {
        super();
        this.data = new Map();
    }

    /**
     * @override
     * @param {string} path 
     * @returns {any}
     */
    readValue(path) {
        const value = this.data.get(path);
        return value !== undefined ? value : null;
    }

    /**
     * @override
     * @param {string} path 
     * @param {any} value 
     */
    saveValue(path, value) {
        if (value === undefined || value === null) {
            this.data.delete(path);
        } else {
            this.data.set(path, value);
        }
    }

    /**
     * @override
     * @param {string} path 
     */
    removeValue(path) {
        this.data.delete(path);
    }

    /**
     * Clear all client storage data
     */
    clear() {
        this.data.clear();
    }
}
