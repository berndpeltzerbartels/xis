/**
 * @typedef PageClientData
 * @property {{string: string}} pathVariables
 * @property {{string: string}} urlParameters
 * @property {Array<Parameter>} parameters;
 * @property {{string: string}} modelData
 */
class PageClientData {

    constructor() {
        this.pathVariables = {};
        this.urlParameters = {};
        this.modelData = {};
        this.parameters = [];
    }

}