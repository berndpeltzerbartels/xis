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
        this.widgetId = undefined;
        this.widget = undefined;
        this.defaultWidgetIdExpression = this.expressionFromAttribute('default-widget');
        this.containerId = tag.getAttribute('container-id'); // TODO validate: the id must not be an expression
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
        this.client.widgetAction(this.widgetId, action, this.widget.data.getValues(keys))
            .then(response => _this.handleActionResponse(response));
    }

    /**
     * @public
     * @param {Data} parentData 
     */
    refresh(parentData) {
        console.log('refresh');
        if (this.defaultWidgetIdExpression) {
            var widgetId = this.defaultWidgetIdExpression.evaluate(parentData);
            this.bindWidget(widgetId);
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
        this.bindWidget(widgetId);
        this.reloadDataAndRefresh(parameters);
    }


    /**
     * @param {string} widgetId 
     * @private
     */
    bindWidget(widgetId) {
        if (widgetId !== this.widgetId) {
            if (this.widget) {
                this.clearChildren();
            }
            this.widgetId = widgetId;
            this.widget = this.widgets.getWidget(this.widgetId);
            this.tag.appendChild(this.widget.root);
        }

    }


    /**
     * @public
     * @param {Array<Parameter>} parameters, may be undefined
     * @param 
     */
    reloadDataAndRefresh(parameters = []) {
        var _this = this;
        var clientData = {};
        if (this.widget.data) {
            for (var dataKey of this.widgets.getModelKeysToSubmitForModel(this.widgetId)) {
                clientData[dataKey] = this.widget.data.getValue([dataKey]);
            }
        }
        var params = {};
        if (parameters) {
            for (var par of parameters) {
                params[par.name] = par.value;
            }
        }
        this.client.loadWidgetData(this.widgetId, clientData, params)
            .then(response => new Data(response.data))
            .then(data => { _this.widget.data = data; return data; })
            .then(data => _this.refreshChildNodes(data))
            .catch(e => console.error(e));

    }

    /**
     * @private
     * @param {Response} response 
     */
    handleActionResponse(response) {
        if (response.nextPageId) {
            debugger
            app.pageController.handleActionResponse(response);
        } else if (response.nextWidgetId) {
            this.bindWidget(response.nextWidgetId);
            this.widget.data = new Data(response.data);
            this.refreshChildNodes(this.widget.data);
        }
    }
}