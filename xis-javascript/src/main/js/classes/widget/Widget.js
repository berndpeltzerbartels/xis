/**
 * @typedef Widget
 * @property {string} id
 * @property {Element}
 * @property {WidgetAttributes} widgetAttributes
 */
class Widget {

    constructor() {
        this.id = undefined;
        this.html = '';
        this.widgetAttributes = {};
        this.urlParameters = {};
    }
}
