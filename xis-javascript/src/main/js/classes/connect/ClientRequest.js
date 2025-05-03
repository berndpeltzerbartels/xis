/**
 * @property {string} clientId
 * @property {string} userId
 * @property {string} pageId
 * @property {string} widgetId 
 * @property {string} action
 * @property {string: string} pathVariables
 * @property {string: string} urlParameters
 * @property {string: string} bindingParameters
 * @property {string: string} type;
 * @property {string: string} clientState
 * @property {string: string} localStorage
 * @property {string: string} localDatabase
*/
class ClientRequest {

    constructor() {
        this.clientId = '';
        this.userId = '';
        this.pageId = '';
        this.widgetId = '';
        this.action = '';
        this.pathVariables = {};
        this.urlParameters = {};
        this.bindingParameters = {};
        this.type = '';
        this.clientStateData = {};
        this.localStorageData = {};
        this.localDatabaseData = {}; // TODO
    }
}