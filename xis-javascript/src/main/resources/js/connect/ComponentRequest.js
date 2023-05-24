/**
 * @property {any} data
 * @property {string} clientId
 * @property {string} userId
 * @property {string} key // action-key or model-key
 * @property {string} controllerId
 * @property {string} type
 */
class ComponentRequest {

    constructor() {
        this.data = {};
        this.clientId = '';
        this.userId = '';
        this.pageId = '';
        this.widgetId = '';
        this.action = '';
    }
}