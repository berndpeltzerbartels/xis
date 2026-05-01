/**
 * @typedef Frontlet
 * @property {string} id
 * @property {Element}
 * @property {FrontletAttributes} widgetAttributes
 */
class Frontlet {

    constructor() {
        this.id = undefined;
        this.html = '';
        this.widgetAttributes = {};
        this.urlParameters = {};
    }
}
