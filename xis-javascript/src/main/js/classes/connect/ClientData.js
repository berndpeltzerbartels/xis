/**
 * @typedef ClientData
 * @property {{string: string}} pathVariables
 * @property {{string: string}} urlParameters
 * @property {{string: string}} modelData
 * @property {string} targetContainerId
 */
class ClientData {

    constructor() {
        this.pathVariables = {};
        this.urlParameters = {};
        this.modelData = {};
        this.targetContainerId = undefined;
    }

    addUrlParameters(urlParameters) {
        for (var key of Object.keys(urlParameters)) {
            this.urlParameters[key] = urlParameters[key];
        }
    }


    addPathVariables(pathVariables) {
        for (var key of Object.keys(pathVariables)) {
            this.pathVariables[key] = pathVariables[key];
        }
    }

}