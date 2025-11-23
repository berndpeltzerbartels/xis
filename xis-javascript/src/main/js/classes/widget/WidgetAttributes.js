/**
 * @typedef WidgetAttributes
 * @property {string} id
 * @property {string} host
 * @property {array<string>} localDatabaseKeys
 * @property {array<string>} localStorageKeys
 * @property {array<string>} sessionStorageKeys
 * @property {array<string>} globalVariableKeys
 */


class WidgetAttributes {

    constructor(obj) {
        this.id = obj.id;
        this.host = obj.host;
        this.localDatabaseKeys = obj.localDatabaseKeys || [];
        this.localStorageKeys = obj.localStorageKeys || [];
        this.sessionStorageKeys = obj.sessionStorageKeys || [];
        this.globalVariableKeys = obj.globalVariableKeys || [];
    }
}