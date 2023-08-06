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
        this.widget = undefined;
        this.defaultWidgetIdExpression = this.expressionFromAttribute('default-widget');
        this.containerId = tag.getAttribute('container-id'); // TODO validate: the id must not be an expression
        this.type = 'widget-container-handler';
        this.clearChildren();
    }

    /**
     * @public
     * @param {String} action 
     * @param {Array<Parameter>} parameters
     */
    submitAction(action, parameters) {
        if (this.widget) {
            var _this = this;
            var clientData = this.widget.clientDataForActionRequest(action, parameters);
            this.client.widgetAction(this.widget.id, clientData, action)
                .then(response => _this.handleActionResponse(response));
        }
    }

    /**
     * @public
     * @param {Data} parentData 
     */
    refresh(parentData) {
        if (this.defaultWidgetIdExpression) {
            var widgetId = this.defaultWidgetIdExpression.evaluate(parentData);
            this.bindWidget(widgetId);
        }
        if (this.widget) {
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
        if (!this.widget || this.widget.id != widgetId) {
            this.bindWidget(widgetId);
        }
        this.reloadDataAndRefresh(parameters);
    }


    /**
     * @private
     * @param {string} widgetId 
     * @private
     */
    bindWidget(widgetId) {
        if (!this.widget || this.widget.id != widgetId) {
            this.clearChildren();
            this.widget = this.widgets.getWidget(widgetId);
            this.tag.appendChild(this.widget.root);
        }
    }

    /**
     * @public
     * @param {Array<Parameter>} parameters, may be undefined
     * @param 
     */
    reloadDataAndRefresh(parameters = []) {
        if (this.widget) {
            var _this = this;
            var clientData = this.widget.clientDataForModelRequest(parameters);
            this.client.loadWidgetData(this.widget.id, clientData, parameters)
                .then(response => new Data(response.data))
                .then(data => { _this.widget.data = data; return data; })
                .then(data => _this.refreshChildNodes(data))
                .catch(e => console.error(e));
        }
    }

    /**
     * @private
     * @param {Response} response 
     */
    handleActionResponse(response) {
        if (response.nextPageURL) {
            debugger
            app.pageController.handleActionResponse(response);
        } else if (response.nextWidgetId) {
            this.bindWidget(response.nextWidgetId);
            this.widget.data = new Data(response.data);
            this.refreshChildNodes(this.widget.data);
        }
    }
}