/**
 * @typedef Widget
 * @property {string} id
 * @property {Element}
 * @property {WidgetAttributes} widgetAttributes
 * @property {Data} data
 */
class Widget {

    constructor() {
        this.id = undefined;
        this.html = '';
        this.widgetAttributes = {};
        this.data = new Data({});
        this.urlParameters = {};
    }
}
