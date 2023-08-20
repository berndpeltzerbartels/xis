/**
 * @typedef WidgetAttributes
 * @property {string} host
 * @property {array<string>} modelParameterNames
 * @property {{string: array<string>}} actionParameterNames
 */


class WidgetAttributes {

    constructor(obj) {
        this.host = obj.host;
        this.modelParameterNames = obj.modelParameterNames || [];
        this.actionParameterNames = obj.actionParameterNames || {};
    }
}