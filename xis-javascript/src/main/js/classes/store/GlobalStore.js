class GlobalStore extends Store {
    constructor(eventPublisher) {
        // GlobalStore doesn't use any real storage area, so we pass null
        super(eventPublisher, null, 'globalStore');
        this.globalData = new Data({});
    }

    /**
     * @override
     * @param {string} path 
     * @returns {string|undefined}
     */
    getValue(path) {
        return this.globalData.getValueByPath(path);
    }

    /**
     * @override
     * @param {string} path 
     * @param {string} value 
     */
    saveData(values) {
        for (var path of Object.keys(values)) {
            const val = values[path];
            if (typeof val === 'function') {
                // Ignore functions
                continue;
            }
            if (val === undefined || val === null) {
                this.removeValue(path);
            } else {
                this.globalData.setValueByPath(path, val);
            }
            this.startUpdate(path);
        }
    }

    /**
     * @override
     * @param {string} path 
     */
    removeValue(path) {
        this.globalData.setValueByPath(path, undefined);
    }

    /**
     * Clear all global data at the end of request processing
     * This should be called after each request is completed
     */
    clear() {
        this.globalData = new Data({});
    }

    /**
     * @override
     * @param {*} event 
     */
    onEvent(event) {
        // Global store doesn't listen to storage events since it's not persisted
        // Storage events are only for localStorage/sessionStorage
    }
}