/**
 * @property {Number} httpStatus
 * @property {any} data
 * @property {string} nextPageId
 * @property {string} nextWidgetId
 */

class Response {

    constructor() {
        this.httpStatus = -1;
        this.data = {};
        this.nextPageId = '';
        this.nextWidgetId = '';
    }
}