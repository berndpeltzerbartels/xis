/**
 * @typedef FrontletAttributes
 * @property {string} id
 * @property {string} url
 * @property {string} host
 * @property {array<string>} localStorageKeys
 * @property {array<string>} sessionStorageKeys
 * @property {array<string>} clientStateKeys
 * @property {array<string>} globalVariableKeys
 * @property {array<string>} updateEventKeys
 */


class FrontletAttributes {

    constructor(obj) {
        this.id = obj.id;
        this.url = obj.url;
        this.host = obj.host;
        this.localStorageKeys = obj.localStorageKeys || [];
        this.sessionStorageKeys = obj.sessionStorageKeys || [];
        this.clientStateKeys = obj.clientStateKeys || [];
        this.globalVariableKeys = obj.globalVariableKeys || [];
        this.updateEventKeys = obj.updateEventKeys || [];
    }
}
