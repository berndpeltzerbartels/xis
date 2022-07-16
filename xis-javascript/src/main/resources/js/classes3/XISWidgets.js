class XISWidgets {

    constructor() {
        this.widgets = {};
    }

    addWidget(key, widget) {
        this.widgets[key] = widget;
    }

    getWidget(key) {
        return this.widgets[key];
    }
    
}