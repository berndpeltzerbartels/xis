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
        console.log('Loading widgets');
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
        console.log('Loading widget ' + widgetId);
        var _this = this;
        var widget = new Widget();
        return this.client.loadWidget(widgetId).then(widgetHtml => {
            console.log('Widget-Html: ' + widgetHtml);
            widget.id = widgetId;
            widget.root = _this.asRootElement(widgetHtml);
            widget.attributes = _this.widgetAttributes[widgetId];
            initialize(widget.root);
            _this.addWidget(widgetId, widget);
        });
    }

    addWidget(widgetId, widget) {
        this.widgets[widgetId] = widget;
    }

    getWidgetRoot(widgetId) {
        return this.widgets[widgetId].root;
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

}
