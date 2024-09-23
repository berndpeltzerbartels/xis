/**
 * @property {Number} status
 * @property {any} data
 * @property {string} nextPageId
 * @property {string} nextWidgetId
 */

class ServerResponse {

    constructor() {
        this.status = -1;
        this.data = {};
        this.nextPageURL = '';
        this.nextWidgetId = '';
    }
}