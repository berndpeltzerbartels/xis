/**
 * @property {string} clientId
 * @property {string} pageId
 * @property {string} pageUrl
 * @property {string} widgetId 
 * @property {string} action
 * @property {string: string} pathVariables
 * @property {string: string} urlParameters
 * @property {string: string} bindingParameters
 * @property {string: string} actionParameters
 * @property {string: string} type;
 * @property {string: string} sessionStorageData
 * @property {string: string} localStorage
 * @property {string: string} localDatabase
*/
class ClientRequest {

    constructor() {
        this.clientId = '';
        this.pageId = '';
        this.pageUrl = '';
        this.widgetId = '';
        this.action = '';
        this.pathVariables = {};
        this.urlParameters = {};
        this.bindingParameters = {};
        this.actionParameters = {};
        this.type = '';
        this.sessionStorageData = {};
        this.localStorageData = {};
        this.globalVariableData = {};
        this.localDatabaseData = {}; // TODO
    }
}