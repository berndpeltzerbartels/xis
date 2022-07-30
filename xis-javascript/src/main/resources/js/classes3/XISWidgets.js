class XISWidgets {

    constructor() {
        this.widgets = {};
    }

    init() {
        var widgets = this.widgets;
        Object.keys(widgets) // Object.values(any) is not supported in many browsers
        .map(key => widgets[key])
        .forEach(widget => widget.init());
    }

    addWidget(key, widget) {
        this.widgets[key] = widget;
    }

    getWidget(key) {
        return this.widgets[key];
    }
    
}