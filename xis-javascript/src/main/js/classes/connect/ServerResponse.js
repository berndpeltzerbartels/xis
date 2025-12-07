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
 * @property {any} sessionStorageData
 * @property {any} clientStorageData
 * @property {any} validatorMessages
 * @property {Object} tagVariables
 * @property {Object} idVariables
 * @property {string} actionProcessing
 * @property {string} title
 * @property {string} address
 */

class ServerResponse {

    constructor() {
        this.status = -1;
        this.data = {};
        this.nextURL = '';
        this.nextWidgetId = '';
        this.formData = {};
        this.localStorageData = {};
        this.globalVariableData = {};
        this.localDatabaseData = {};
        this.widgetContainerId = '';
        this.reloadWidgets = [];
        this.sessionStorageData = {};
        this.clientStorageData = {};
        this.validatorMessages = { };
        this.actionProcessing = 'NONE';
        this.updateEventKeys = [];
        this.annotatedTitle = undefined;
        this.annotatedAddress = undefined;
        this.defaultWidgets = [];
    }


    containsGlobals() {
       if (this.globalVariableData && Object.keys(this.globalVariableData).length > 0) {
           return true;
       }
       if (this.localStorageData && Object.keys(this.localStorageData).length > 0) {
           return true;
       }
       if (this.localDatabaseData && Object.keys(this.localDatabaseData).length > 0) {
           return true;
       }
       if (this.sessionStorageData && Object.keys(this.sessionStorageData).length > 0) {
           return true;
       }
       if (this.clientStorageData && Object.keys(this.clientStorageData).length > 0) {
           return true;
       }
       return false;
    }
}


