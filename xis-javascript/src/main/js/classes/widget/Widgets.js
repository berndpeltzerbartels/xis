class Widgets {

    /**
     *
     * @param {Client} client
     */
    constructor(client) {
        this.widgets = {};
        this.widgetInstances = {};
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
            widget.html = widgetHtml;
            widget.widgetAttributes = _this.widgetAttributes[widgetId];
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
    * @returns {WidgetInstance}
    */
    getWidgetInstance(widgetId) {
        if (!this.widgetInstances[widgetId]) {
            this.widgetInstances[widgetId] = [];
        }
        var instances = this.widgetInstances[widgetId];
        var widgetInstance = instances.shift();
        if (!widgetInstance) {
            var widget = this.widgets[widgetId];
            if (!widget) {
                throw new Error('no such widget: ' + widgetId);
            }
            widgetInstance = new WidgetInstance(widget, this);
        }
        return widgetInstance;
    }

    /**
     * @public
     * @param {WidgetInstance} widgetInstance 
     */
    disposeInstance(widgetInstance) {
        var instances = this.widgetInstances[widgetInstance.widget.id];
        instances.push(widgetInstance);
    }

    reset() {
        this.widgets = {};
        this.widgetInstances = {};
        this.widgetAttributes = {};
    }

}
