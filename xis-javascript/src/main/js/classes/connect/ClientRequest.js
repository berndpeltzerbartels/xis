/**
 * @property {any} data
 * @property {string} clientId
 * @property {string} userId
 * @property {string} pageId
 * @property {string} widgetId 
 * @property {string} action
 * @property {string: string} pathVariables
 * @property {string: string} parameters
 * @property {string: string} urlParameters
*/
class ClientRequest {

    constructor() {
        this.data = {};
        this.clientId = '';
        this.userId = '';
        this.pageId = '';
        this.widgetId = '';
        this.action = '';
        this.pathVariables = {};
        this.parameters = {};
        this.urlParameters = {};

    }
}