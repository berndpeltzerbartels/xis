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
        this.widgeteData = {};
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
        if (!this.widgetId && this.tag.getAttribute('default-widget')) {
            var widgetId = this.defaultWidgetIdExpression.evaluate(data);
            this.showWidget(widgetId);
        }
        this.widgeteData[this.widgetId] = data;
    }

    /**
     * @public
     * @param {string} widgetId 
     * @returns {Promise<void>}
     */
    showWidget(widgetId) {
        if (widgetId !== this.widgetId) {
            if (this.widgetRoot) {
                this.clearChildren();
            }
            this.widgetId = widgetId;
            this.widgetRoot = this.widgets.getWidgetRoot(this.widgetId);
            this.tag.appendChild(this.widgetRoot);
        }
    }


    /**
     * @public
     */
    reloadDataAndRefresh() {
        var _this = this;
        var clientData = {};
        var widgetData = this.widgeteData[this.widgetId];
        if (widgetData) {
            for (var dataKey of this.widgets.getModelKeysToSubmitForModel(this.widgetId)) {
                clientData[dataKey] = widgetData.getValue([dataKey]);
            }
        }
        this.client.loadWidgetData(this.widgetId, clientData)
            .then(response => new Data(response.data))
            .then(data => { _this.data[_this.widgetId] = data; return data; })
            .then(data => _this.refreshChildNodes(data));

    }

    /**
     * @private
     * @param {Response} response 
     */
    handleActionResponse(response) {
        if (response.nextPageId) {
            bindPage(response.nextPageId)
                .then(() => reloadDataAndRefreshCurrentPage());
        } else if (response.nextWidgetId) {
            this.showWidget(response.nextWidgetId)
            this.reloadDataAndRefresh();
        }
    }
}