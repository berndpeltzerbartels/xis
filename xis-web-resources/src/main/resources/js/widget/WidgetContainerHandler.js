class WidgetContainerHandler extends TagHandler {

    /**
     *
     * @param {Element} tag
     * @param {WidgetService} widgetService
     */
    constructor(tag, widgetService) {
        super(tag);
        this.widgetService = widgetService;
        this.parent = this.findParentHtmlElement();
        this.initialWidgetId = this.getAttribute('widget');
        this.widgetRoot;
        this.clearChildren();
    }

    refresh(data) {
        this.ensureWidgetPresent();
        if (this.widgetRoot.refresh) {
            this.widgetRoot.refresh(data);
        }
    }

    ensureWidgetPresent() {
        if (!this.widgetRoot) {
            this.widgetRoot = this.getWidgetRoot(this.widgetId);
            this.showWidget(this.widgetRoot);
        }
    }

    showWidget(widgetRoot) {
        if (this.parent.nextSibling) {
            this.parent.insertBefore(widgetRoot, this.parent.nextSibling);
        } else {
            this.parent.appendChild(widgetRoot);
        }
    }


    getWidgetRoot(widgetId) {
        return this.widgetService.getWidget(widgetId).root;
    }

}