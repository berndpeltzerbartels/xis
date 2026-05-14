/**
 * @property {Number} status
 * @property {any} data
 * @property {string} nextURL
 * @property {string} nextFrontletId
 * @property {string} nextModalId
 * @property {boolean} closeModal
 * @property {boolean} reloadModalParent
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
 * @property {boolean} authenticated
 * @property {Array<string>} userRoles
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
        this.nextModalId = '';
        this.closeModal = false;
        this.reloadModalParent = false;
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
        this.authenticated = false;
        this.userRoles = [];
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
