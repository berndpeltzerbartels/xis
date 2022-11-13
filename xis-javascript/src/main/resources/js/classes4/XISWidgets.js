class XISWidgets {

    constructor() {
        this.className = 'XISWidgets';
        this.widgets = {};
    }


    addWidget(key, widget) {
        this.widgets[key] = widget;
    }

    getWidget(key) {
        var widget =  this.widgets[key];
        if (!widget.initialized) {
            widget.init();
            widget.initialized = true;
        }
        return widget;
    }
    
}