/**
 * @property {any} data
 * @property {string} clientId
 * @property {string} userId
 * @property {string} pageId
 * @property {string} widgetId 
 * @property {string} action
 * @property {string} nextPageId
 * @property {string} nextWidgetId 
*/
class Request {

    constructor() {
        this.data = {};
        this.clientId = '';
        this.userId = '';
        this.pageId = '';
        this.widgetId = '';
        this.action = '';
        this.nextPageId = '';
        this.nextWidgetId = '';
    }
}