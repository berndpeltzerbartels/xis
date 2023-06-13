class WidgetContainerHandler extends TagHandler {

    /**
     *
     * @param {Element} tag
     * @param {Client} client
     * @param {Widgets} widgets
     */
    constructor(tag, client, widgets) {
        super(tag);
        this.client = client;
        this.widgets = widgets;
        this.defaultWidgetIdExpression = this.expressionFromAttribute('default-widget');
        this.containerId = tag.getAttribute('container-id'); // TODO validate: the id must not be an expression
        this.widgetRoot = undefined;
        this.widgetData = {};
        this.type = 'widget-container-handler';
        this.clearChildren();
    }

    /**
     * @public
     * @param {String} action 
     */
    submitAction(action) {
        var _this = this;
        var keys = this.widgets.getModelKeysToSubmitForAction(this.widgetId, action);
        this.client.widgetAction(this.widgetId, action, this.widgeteData.getValues(keys))
            .then(response => _this.handleActionResponse(response));
    }

    /**
     * @public
     * @param {Data} data 
     */
    refresh(data) {
        console.log('refresh');
        if (!this.widgetId && this.defaultWidgetIdExpression) {
            this.widgetId = this.defaultWidgetIdExpression.evaluate(data);
            this.doShowWidget();
        }
        if (this.widgetId) {
            this.reloadDataAndRefresh();
        }
    }

    /**
     * @public
     * @param {string} widgetId 
     * @param {Array<Parameter>} parameters
     * @returns {Promise<void>}
     */
    showWidget(widgetId, parameters = []) {
        if (widgetId !== this.widgetId) {
            if (this.widgetRoot) {
                this.clearChildren();
            }
            this.widgetId = widgetId;
        }
        this.doShowWidget();
        this.reloadDataAndRefresh(parameters);
    }


    doShowWidget() {
        this.widgetRoot = this.widgets.getWidgetRoot(this.widgetId);
        this.tag.appendChild(this.widgetRoot);
    }


    /**
     * @public
     * @param {Array<Parameter>} parameters, may be undefined
     * @param 
     */
    reloadDataAndRefresh(parameters = []) {
        var _this = this;
        var clientData = {};
        var widgetData = this.widgetData[this.widgetId];
        if (widgetData) {
            for (var dataKey of this.widgets.getModelKeysToSubmitForModel(this.widgetId)) {
                clientData[dataKey] = widgetData.getValue([dataKey]);
            }
        }
        var params = {};
        if (parameters) {
            for (var par of parameters) {
                params[pageAttributes.name] = par.value;
            }
        }
        this.client.loadWidgetData(this.widgetId, clientData, params)
            .then(response => new Data(response.data))
            .then(data => { _this.widgetData[_this.widgetId] = data; return data; })
            .then(data => _this.refreshChildNodes(data))
            .catch(e => console.error(e));

    }

    /**
     * @private
     * @param {Response} response 
     */
    handleActionResponse(response) {
        if (response.nextPageId) {
            displayPage(response.nextPageId);
        } else if (response.nextWidgetId) {
            this.showWidget(response.nextWidgetId)
        }
    }
}