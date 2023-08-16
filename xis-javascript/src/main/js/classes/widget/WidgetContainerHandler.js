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
        this.widget = undefined;
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
            this.bindWidget(response.nextWidgetId);
        }
        this.widget.data = response.data;
        this.refreshChildNodes(this.widget.data);
    }

    /**
     * @public
     * @param {Data} parentData 
     */
    refresh(parentData) {
        this.refreshContainerId(parentData);
        this.refreshDefaultWidget(parentData);
        if (this.widget) {
            this.widget.data = parentData;
            this.reloadDataAndRefresh({});
        }
    }

    /**
     * @public
     * @param {string} widgetId 
     * @param {{string: any}} widgetUrlParameters
     * @returns {Promise<void>}
     */
    showWidget(widgetId, widgetUrlParameters = {}) {
        if (!this.widget || this.widget.id != widgetId) {
            this.bindWidget(widgetId);
        }
        this.reloadDataAndRefresh(widgetUrlParameters);
    }

    /**
     * @public
     * @returns {string}
     */
    getCurrentWidgetId() {
        return this.widget ? this.widget.id : undefined;
    }


    getPageData() {
        return app.pageController.data;
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
        if (this.defaultWidgetIdExpression && !this.widget) { // once, only
            var widgetId = this.defaultWidgetIdExpression.evaluate(parentData);
            this.bindWidget(widgetId);
        }
    }

    /**
     * @private
     * @param {string} widgetId 
     */
    bindWidget(widgetId) {
        if (!this.widget || this.widget.id != widgetId) {
            this.clearChildren();
            this.widget = assertNotNull(this.widgets.getWidget(widgetId), 'no such widget: ' + widgetId);
            this.tag.appendChild(this.widget.root);
        }
    }

    /**
     * @private
     * @param {{string: any}} widgetUrlParameters 
     */
    reloadDataAndRefresh(widgetUrlParameters) {
        if (this.widget) {
            var _this = this;
            var clientData = this.widget.clientDataForModelRequest();
            clientData.addUrlParameters(widgetUrlParameters);
            this.client.loadWidgetData(this.widget.id, clientData)
                .then(response => response.data)
                .then(data => { _this.widget.data = data; return data; })
                .then(data => _this.refreshChildNodes(data))
                .catch(e => console.error(e));
        }
    }
}