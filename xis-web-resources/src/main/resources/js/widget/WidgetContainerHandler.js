class WidgetContainerHandler extends TagHandler {

    /**
     *
     * @param {Element} tag
     * @param {Widgets} widgets
     */
    constructor(tag, widgets) {
        super(tag);
        this.widgets = widgets;
        this.parent = this.findParentHtmlElement();
        this.initialWidgetId = this.getAttribute('widget');
        this.widgetRoot;
        this.clearChildren();
    }

    refresh(data) {
        console.log('refresh');
        this.ensureWidgetPresent();
        if (this.widgetRoot._refresh) {
            this.widgetRoot._refresh(data);
        }
    }

    ensureWidgetPresent() {
        console.log('ensureWidgetPresent');
        if (!this.widgetRoot) {
            this.widgetRoot = this.getWidgetRoot(this.widgetId);
            this.showWidget(this.widgetRoot);
        }
    }

    showWidget(widgetRoot) {
        console.log('showWidget');
        this.tag.appendChild(widgetRoot);
    }


    getWidgetRoot(widgetId) {
        return this.widgets.getWidget(widgetId).root;
    }

}