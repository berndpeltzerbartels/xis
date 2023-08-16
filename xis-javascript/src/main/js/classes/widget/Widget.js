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
        this.root = undefined;
        this.widgetAttributes = {};
        this.data = new Data({});
        this.urlParameters = {};
    }

    /**
    * @public
    * @returns {ClientData}
    */
    clientDataForModelRequest() {
        debugger;
        var resolvedURL = app.pageController.resolvedURL;
        var clientData = new ClientData();
        clientData.addPathVariables(resolvedURL.pathVariables);
        clientData.addUrlParameters(resolvedURL.urlParameters);
        clientData.addUrlParameters(this.urlParameters); // overriding is allowed
        for (dataKey of this.widgetAttributes.modelsToSubmitOnRefresh) {
            clientData.modelData[dataKey] = this.data.getValue([dataKey]);
        }
        return clientData;
    }


    /**
    * @public
    * @param {string} action
    * @param {{string: any}} parameters passed by parameter-tag
    * @returns {WidgetClientData}
    */
    clientDataForActionRequest(action, targetContainerId) {
        var resolvedURL = app.pageController.resolvedURL;
        var clientData = new ClientData();
        clientData.addPathVariables(resolvedURL.pathVariables);
        clientData.addUrlParameters(resolvedURL.urlParameters);
        clientData.addUrlParameters(this.urlParameters); // overriding is allowed
        clientData.targetContainerId = targetContainerId;
        var keys = this.widgetAttributes.modelsToSubmitOnAction[action];
        for (var dataKey of keys) {
            clientData.modelData[dataKey] = this.data.getValue([dataKey]);
        }
        return clientData;
    }
}
