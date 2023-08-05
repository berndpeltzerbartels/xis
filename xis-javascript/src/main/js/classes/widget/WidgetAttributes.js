/**
 * @typedef WidgetAttributes
 * @property {string} host
 * @property {array<string>} modelsToSubmitOnRefresh
 * @property {{string: array<string>}} modelsToSubmitOnAction
 */


class WidgetAttributes {

    constructor(obj) {
        this.host = obj.host;
        this.modelsToSubmitOnRefresh = obj.modelsToSubmitOnRefresh;
        this.modelsToSubmitOnAction = obj.modelsToSubmitOnAction;
    }
}