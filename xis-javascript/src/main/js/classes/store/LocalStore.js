class LocalStore extends Store{
    constructor() {
        super();
    }

    /**
     * @override
     * @param {string} path 
     * @returns {any}
     */
    readValue(path) {
        return this.localStorage.getItem(path);
    }

    /**
     * @override
     * @param {*} path 
     * @param {*} value 
     */
    saveValue(path, value) {
        localStorage.setItem(path, value);
    }
}