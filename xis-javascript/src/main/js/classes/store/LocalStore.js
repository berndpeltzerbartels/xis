class LocalStore extends Store{
    constructor() {
        super(localStorage);
    }

    /**
     * @override
     * @param {string} path 
     * @returns {any}
     */
    readValue(path) {
        return localStorage.getItem(path);
    }

    /**
     * @override
     * @param {*} path 
     * @param {*} value 
     */
    saveValue(path, value) {
        if (value === undefined || value === null) {
            localStorage.removeItem(path);
        } else {
            localStorage.setItem(path, value);
        }
    }

    /**
     * @override
     * @param {string} path 
     */
    removeValue(path) {
        localStorage.removeItem(path);
    }
}