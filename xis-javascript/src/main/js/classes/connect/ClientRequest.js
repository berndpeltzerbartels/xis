/**
 * @property {string} clientId
 * @property {string} pageId
 * @property {string} pageUrl
 * @property {string} frontletId 
 * @property {string} action
 * @property {string: string} pathVariables
 * @property {string: string} urlParameters
 * @property {string: string} bindingParameters
 * @property {string: string} actionParameters
 * @property {string: string} type;
 * @property {string: string} sessionStorageData
 * @property {string: string} clientStorageData
 * @property {string: string} localStorage
 * @property {string: string} localDatabase
*/
class ClientRequest {

    constructor() {
        this.clientId = '';
        this.pageId = '';
        this.pageUrl = '';
        this.frontletId = '';
        this.action = '';
        this.pathVariables = {};
        this.urlParameters = {};
        this.bindingParameters = {};
        this.actionParameters = {};
        this.type = '';
        this.sessionStorageData = {};
        this.clientStorageData = {};
        this.localStorageData = {};
        this.globalVariableData = {};
        this.localDatabaseData = {}; // TODO
        this.frontletParameters = {};
        this.frontletContainerId = '';
    }

    toJSON() {
        const compact = {};
        for (const key of Object.keys(this)) {
            const value = this[key];
            if (this.hasPayload(value)) {
                compact[key] = value;
            }
        }
        return compact;
    }

    hasPayload(value) {
        if (value === undefined || value === null || value === '') {
            return false;
        }
        if (Array.isArray(value)) {
            return value.length > 0;
        }
        if (typeof value === 'object') {
            return Object.keys(value).length > 0;
        }
        return true;
    }
}
