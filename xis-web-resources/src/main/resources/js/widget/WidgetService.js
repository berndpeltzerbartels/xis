class WidgetService {

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
            widget.attributes = _this.widgetAttributes[widgetId];
            _this.widgets[widgetId] = widget;
        });
    }

    getWidgetRoot(widgetId) {
        return this.widgets[widgetId].root;
    }


    /**
     *
     * @param {string} tree
     * @returns {Element}
     */
    asRootElement(tree) {
        var div = createElement('div');
        div.innerHTML = trim(tree);
        return div.childNodes.item(0);
    }

}
