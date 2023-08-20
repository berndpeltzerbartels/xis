class WidgetContainerHandler extends TagHandler {

    /**
     *
     * @param {Element} tag
     * @param {Client} client
     * @param {Widgets} widgets
     * @param {WidgetContainers} widgetContainers
     */
    constructor(tag, client, widgets, widgetContainers) {
        super(tag);
        this.client = client;
        this.widgets = widgets;
        this.widgetContainers = widgetContainers;
        this.widgetInstance = undefined;
        this.containerId = undefined;
        this.containerIdExpression = this.expressionFromAttribute('container-id');
        this.defaultWidgetExpression = this.expressionFromAttribute('default-widget');
        this.type = 'widget-container-handler';
    }

    /**
    * @public
    * @param {Response} response 
    */
    handleActionResponse(response) {
        if (response.nextWidgetId) {
            this.ensureWidgetBound(response.nextWidgetId);
        }
        if (!this.widgetState) {
            this.widgetState = new WidgetState(app.pageController.resolvedURL, {});
        }
        var data = response.data;
        this.widgetState.data = data;
        this.refreshContainerId(data);
        this.refreshChildNodes(data);
    }

    /**
     * @public
     * @param {Data} data 
     */
    refresh(data) {
        this.refreshContainerId(data);
        this.refreshDefaultWidget(data);
        var widgetParameters = this.widgetState ? this.widgetState.widgetParameters : {};
        this.widgetState = new WidgetState(app.pageController.resolvedURL, widgetParameters);
        if (this.widgetInstance) {
            this.widgetState.data = data;
            this.reloadDataAndRefresh();
        }
    }

    /**
     * @public
     * @param {string} widgetId 
     * @param {WidgetState} widgetState
     * @returns {Promise<void>}
     */
    showWidget(widgetId, widgetState) {
        this.widgetState = widgetState;
        this.ensureWidgetBound(widgetId);
        this.widgetState = widgetState;
        this.reloadDataAndRefresh();
    }


    /**
     * @private
     * @param {Data} parentData 
     */
    refreshContainerId(parentData) {
        if (this.containerIdExpression) {
            var containerId = this.containerIdExpression.evaluate(parentData);
            if (this.containerId) {
                this.widgetContainers.updateContainerId(this.containerId, containerId);
            } else {
                this.widgetContainers.addContainer(this.tag, containerId);
            }
            this.containerId = containerId;
        }
    }

    /**
     * @private
     * @param {Data} parentData 
     */
    refreshDefaultWidget(parentData) {
        if (this.defaultWidgetExpression && !this.widgetInstance) { // once, only
            var widgetUrl = this.defaultWidgetExpression.evaluate(parentData);
            var widgetParameters = urlParameters(widgetUrl);
            var widgetId = stripQuery(widgetUrl);
            this.ensureWidgetBound(widgetId);
            this.widgetState = new WidgetState(app.pageController.resolvedURL, widgetParameters);
        }
    }

    /**
     * @private
     * @param {string} widgetId
     */
    ensureWidgetBound(widgetId) {
        if (this.widgetInstance) {
            if (this.widgetInstance.widget.id == widgetId) {
                return;
            } else {
                this.clearChildren();
                this.widgetInstance.dispose();
            }
        }
        this.widgetInstance = assertNotNull(this.widgets.getWidgetInstance(widgetId), 'no such widget: ' + widgetId);
        this.tag.appendChild(this.widgetInstance.root);
    }

    /**
     * @private
     */
    reloadDataAndRefresh() {
        if (this.widgetInstance) {
            var resolvedURL = this.widgetState.resolvedURL;
            var _this = this;
            this.client.loadWidgetData(this.widgetInstance, this.widgetState)
                .then(response => response.data)
                .then(data => { data.setValue('urlParameters', resolvedURL.urlParameters); return data; })
                .then(data => { data.setValue('pathVariables', resolvedURL.pathVariablesAsMap()); return data; })
                .then(data => { data.setValue('widgetParameters', _this.widgetState.widgetParameters); return data; })
                .then(data => { _this.widgetState.data = data; return data; })
                .then(data => _this.refreshChildNodes(data))
                .catch(e => console.error(e));
        }
    }
}