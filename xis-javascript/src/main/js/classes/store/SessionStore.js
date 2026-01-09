class SessionStore extends Store {
    constructor(eventPublisher) {
        super(eventPublisher, sessionStorage, 'sessionStorageUpdated');
    }

    /**
     * @override
     * @param {string} path 
     * @returns {any}
     */
    readValue(path) {
    debugger;
        return sessionStorage.getItem(path);
    }


    /**
     * @override
     * @param {string} path 
     * @param {any} value 
     */
    saveValue(path, value) {
    debugger;
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