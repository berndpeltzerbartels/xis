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
    }

    /**
     * @public
     * @param {{string: any}} parameters passed by parameter-tag
     * @returns {WidgetClientData}
     */
    clientDataForModelRequest(parameters) {
        var clientData = this.clientData(parameters);
        this.addModelDataForModelRequest(clientData);
        return clientData;
    }

    /**
   * @public
   * @param {string} action
   * @param {{string: any}} parameters passed by parameter-tag
   * @param {string} targetContainerId
   * @returns {WidgetClientData}
   */
    clientDataForActionRequest(action, parameters, targetContainerId) {
        var clientData = this.clientData(parameters);
        clientData.targetContainerId = targetContainerId;
        this.addModelDataForActionRequest(action, clientData);
        return clientData;
    }

    /** 
  * @private
  * @param {WidgetClientData} clientData
  */
    addModelDataForModelRequest(clientData) {
        for (var dataKey of Object.keys(this.widgetAttributes.modelsToSubmitOnRefresh)) {
            clientData.modelData[dataKey] = this.data.getValue([dataKey]);
        }
    }

    /** 
    * @private
    * @param {WidgetClientData} clientData
    */
    addModelDataForActionRequest(action, clientData) {
        for (var dataKey of this.widgetAttributes.modelsToSubmitOnAction[action]) {
            clientData.modelData[dataKey] = this.data.getValue([dataKey]);
        }
    }

    /**
    * @private
    * @param {{string: any}} parameters created by parameter-tag
    * @returns {WidgetClientData}
    */
    clientData(parameters) {
        var clientData = new WidgetClientData();
        clientData.parameters = parameters;
        return clientData;
    }
}
