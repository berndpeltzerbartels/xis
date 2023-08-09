/**
 * @property {Number} httpStatus
 * @property {any} data
 * @property {string} nextPageId
 * @property {string} nextWidgetId
 */

class ServerResponse {

    constructor() {
        this.httpStatus = -1;
        this.data = {};
        this.nextPageURL = '';
        this.nextWidgetId = '';
    }
}