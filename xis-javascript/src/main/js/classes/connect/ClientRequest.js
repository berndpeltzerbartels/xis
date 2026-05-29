/**
 * @property {string} clientId
 * @property {string} pageId
 * @property {string} pageUrl
 * @property {string} frontletId 
 * @property {string} frontletContainerId
 * @property {string} action
 * @property {string} formBinding
 * @property {any} formData
 * @property {string: string} pathVariables
 * @property {string: string} urlParameters
 * @property {string: string} queryParameters
 * @property {string: string} actionParameters
 * @property {string: string} frontletParameters
 * @property {string: string} modalParameters
 * @property {string} load
 * @property {string} zoneId
 * @property {string} locale
 * @property {string} accessToken
 * @property {string} renewToken
 * @property {string: string} type;
 * @property {string: string} sessionStorageData
 * @property {string: string} clientStateData
 * @property {string: string} localStorageData
 * @property {string: string} globalVariableData
*/
class ClientRequest {

    constructor() {
        this.clientId = '';
        this.pageId = '';
        this.pageUrl = '';
        this.frontletId = '';
        this.frontletContainerId = '';
        this.action = '';
        this.formBinding = '';
        this.formData = {};
        this.pathVariables = {};
        this.urlParameters = {};
        this.queryParameters = {};
        this.actionParameters = {};
        this.frontletParameters = {};
        this.modalParameters = {};
        this.load = 'INITIAL';
        this.zoneId = '';
        this.locale = '';
        this.accessToken = '';
        this.renewToken = '';
        this.type = '';
        this.sessionStorageData = {};
        this.clientStateData = {};
        this.localStorageData = {};
        this.globalVariableData = {};
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
