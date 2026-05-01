class FrontletInstance {

    /**
     * @param {Frontlet} widget
     * @param {Frontlets} widgets
     */
    constructor(widget, widgets) {
        this.widget = widget;
        this.widgets = widgets;
        this.root = assertNotNull(normalizeElement(htmlToElement(widget.html)));
        this.rootHandler = assertNotNull(initializeElement(this.root));
    }

    dispose() {
        this.widgets.disposeInstance(this);
    }


}
