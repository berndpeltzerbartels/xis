class WidgetInstance {

    /**
     * @param {Widget} widget 
     * @param {Widgets} widgets
     */
    constructor(widget, widgets) {
        this.widget = widget;
        this.widgets = widgets;
        this.root = htmlToElement(widget.html);
        initializeElement(this.root);
    }

    dispose() {
        this.widgets.disposeInstance(this);
    }


}