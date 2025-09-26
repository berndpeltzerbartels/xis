class ClientState extends Store {
    constructor() {
        super();
    }

    /**
     * @override
     * @param {string} path 
     * @returns {any}
     */
    readValue(path) {
        return sessionStorage.getItem(path);
    }


    /**
     * @override
     * @param {string} path 
     * @param {any} value 
     */
    saveValue(path, value) {
        sessionStorage.setItem(path, value);
    }


    /**
     * @override
     * @param {string} path 
     */
    removeValue(path) {
        sessionStorage.removeItem(path);
    }

}