/**
 * @property {Number} status
 * @property {any} data
 * @property {string} nextURL
 * @property {string} nextWidgetId
 * @property {any} formData
 * @property {any} localStorageData
 * @property {any} localDatabaseData
 * @property {string} widgetContainerId
 * @property {Array} reloadWidgets
 * @property {any} clientStateData
 * 
 */

class ServerResponse {

    constructor() {
        this.status = -1;
        this.data = {};
        this.nextURL = '';
        this.nextWidgetId = '';
        this.formData = {};
        this.localStorageData = {};
        this.localDatabaseData = {};
        this.widgetContainerId = '';
        this.reloadWidgets = [];
        this.clientStateData = {};
    }
}


