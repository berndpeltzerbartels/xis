/**
 * @property {Number} status
 * @property {any} data
 * @property {string} nextURL
 * @property {string} nextFrontletId
 * @property {any} formData
 * @property {any} localStorageData
 * @property {any} localDatabaseData
 * @property {any} globalVariableData
 * @property {Object} frontletParameters
 * @property {string} frontletContainerId
 * @property {Array} reloadFrontlets
 * @property {any} sessionStorageData
 * @property {any} clientStorageData
 * @property {any} validatorMessages
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
        this.nextFrontletId = '';
        this.formData = {};
        this.localStorageData = {};
        this.globalVariableData = {};
        this.localDatabaseData = {};
        this.frontletParameters = {};
        this.frontletContainerId = '';
        this.reloadFrontlets = [];
        this.sessionStorageData = {};
        this.clientStorageData = {};
        this.validatorMessages = { };
        this.actionProcessing = 'NONE';
        this.updateEventKeys = [];
        this.annotatedTitle = undefined;
        this.annotatedAddress = undefined;
        this.defaultFrontlets = [];
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

