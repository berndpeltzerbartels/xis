class Widgets {

    /**
     *
     * @param {Client} client
     */
    constructor(client) {
        this.widgets = {};
        this.client = client;
        this.widgetAttributes = {};
    }

    loadWidgets(config) {
        this.widgetAttributes = config.widgetAttributes;
        var _this = this;
        var promises = [];
        this.widgetAttributes = config.widgetAttributes;
        config.widgetIds.forEach(id => _this.widgets[id] = {});
        config.widgetIds.forEach(id => promises.push(_this.loadWidget(id)));
        return Promise.all(promises).then(() => config);
    }
    /**
    * @returns {Promise<string>}
    */
    loadWidget(widgetId) {
        var _this = this;
        return this.client.loadWidget(widgetId).then(widgetHtml => {
            var widget = new Widget();
            widget.id = widgetId;
            widget.root = _this.asRootElement(widgetHtml);
            widget.root._widgetId = widgetId;
            widget.widgetAttributes = _this.widgetAttributes[widgetId];
            initializeElement(widget.root);
            _this.addWidget(widgetId, widget);
        });
    }

    /**
     * @public
     * @param {string} widgetId 
     * @param {Widget} widget 
     */
    addWidget(widgetId, widget) {
        this.widgets[widgetId] = widget;
    }

    /**
    * @public
    * @param {string} widgetId 
    * @returns {Widget}
    */
    getWidget(widgetId) {
        return this.widgets[widgetId];
    }


    getModelKeysToSubmitForModel(widgetId) {
        return this.widgetAttributes[widgetId].modelsToSubmitOnRefresh;
    }

    getModelKeysToSubmitForAction(widgetId, action) {
        return this.widgetAttributes[widgetId].modelsToSubmitOnAction[action];
    }

    /**
     *
     * @param {string} tree
     * @returns {Element}
     */
    asRootElement(tree) {
        return htmlToElement(tree);
    }

    reset() {
        this.widgets = {};
        this.widgetAttributes = {};
    }

}
