/**
 * @property {any} data
 * @property {string} clientId
 * @property {string} userId
 * @property {string} pageId
 * @property {string} widgetId 
 * @property {string} action
 * @property {string: string} pathVariables
 * @property {string: string} urlParameters
 * @property {string: string} widgetParameters
*/
class ClientRequest {

    constructor() {
        this.data = {};
        this.formData = {};
        this.clientId = '';
        this.userId = '';
        this.pageId = '';
        this.widgetId = '';
        this.action = '';
        this.pathVariables = {};
        this.urlParameters = {};
        this.widgetParameters = {};

    }
}