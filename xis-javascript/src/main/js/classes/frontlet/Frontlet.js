/**
 * @typedef Frontlet
 * @property {string} id
 * @property {Element}
 * @property {FrontletAttributes} frontletAttributes
 */
class Frontlet {

    constructor() {
        this.id = undefined;
        this.html = '';
        this.frontletAttributes = {};
        this.urlParameters = {};
    }
}
