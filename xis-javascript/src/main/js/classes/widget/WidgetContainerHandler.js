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
        this.defaultWidgetIdExpression = this.expressionFromAttribute('default-widget');
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
        var widgetParameters = response.widgetParameters ? response.widgetParameters : {};
        this.widgetInstance.widgetState = new WidgetState(app.pageController.resolvedURL, widgetParameters);
        this.widgetInstance.widgetState.data = response.data;
        this.refreshChildNodes(this.widgetInstance.widgetState.data);
    }

    /**
     * @public
     * @param {Data} data 
     */
    refresh(data) {
        this.refreshContainerId(data);
        this.refreshDefaultWidget(data);
        if (this.widgetInstance) {
            this.widgetInstance.widgetState.data = data;
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
        this.ensureWidgetBound(widgetId);
        this.widgetInstance.widgetState = widgetState;
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
        if (this.defaultWidgetIdExpression && !this.widgetInstance) { // once, only
            var widgetId = this.defaultWidgetIdExpression.evaluate(parentData);
            this.ensureWidgetBound(widgetId);
            this.widgetInstance.widgetState = new WidgetState(app.pageController.resolvedURL, {});
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
            var _this = this;
            this.client.loadWidgetData(this.widgetInstance)
                .then(response => response.data)
                .then(data => { _this.widgetInstance.widgetState.data = data; return data; })
                .then(data => _this.refreshChildNodes(data))
                .catch(e => console.error(e));
        }
    }
}