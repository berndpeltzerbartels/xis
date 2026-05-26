/**
 * @property {Number} status
 * @property {any} data
 * @property {string} nextURL
 * @property {string} nextFrontletId
 * @property {string} nextModalId
 * @property {boolean} closeModal
 * @property {boolean} reloadModalParent
 * @property {any} formData
 * @property {Array<string>} returnedFormDataKeys
 * @property {any} localStorageData
 * @property {any} localDatabaseData
 * @property {any} globalVariableData
 * @property {Object} frontletParameters
 * @property {Object} modalParameters
 * @property {string} frontletContainerId
 * @property {Array} reloadFrontlets
 * @property {Array} updateEventKeys
 * @property {any} sessionStorageData
 * @property {any} clientStateData
 * @property {any} validatorMessages
 * @property {boolean} authenticated
 * @property {Array<string>} userRoles
 * @property {Object} idVariables
 * @property {boolean} reloadPage
 * @property {string} redirectUrl
 * @property {string} actionProcessing
 * @property {string} annotatedTitle
 * @property {string} annotatedAddress
 * @property {Array} defaultFrontlets
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
        this.returnedFormDataKeys = [];
        this.localStorageData = {};
        this.globalVariableData = {};
        this.localDatabaseData = {};
        this.frontletParameters = {};
        this.modalParameters = {};
        this.frontletContainerId = '';
        this.reloadFrontlets = [];
        this.sessionStorageData = {};
        this.clientStateData = {};
        this.validatorMessages = { };
        this.authenticated = false;
        this.userRoles = [];
        this.idVariables = {};
        this.reloadPage = false;
        this.redirectUrl = '';
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
       if (this.clientStateData && Object.keys(this.clientStateData).length > 0) {
           return true;
       }
       return false;
    }
}
