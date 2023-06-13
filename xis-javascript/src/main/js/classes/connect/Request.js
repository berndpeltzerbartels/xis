/**
 * @property {any} data
 * @property {string} clientId
 * @property {string} userId
 * @property {string} pageId
 * @property {string} widgetId 
 * @property {string} action
*/
class Request {

    constructor() {
        this.data = {};
        this.clientId = '';
        this.userId = '';
        this.pageId = '';
        this.widgetId = '';
        this.action = '';
    }
}