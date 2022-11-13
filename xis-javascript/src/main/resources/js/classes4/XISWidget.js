class XISWidget extends XISComponent {

    constructor() {
        super(undefined);
        this.className = 'XISWidget';

    }

    replace(id) {
        var container = this.parent;
        var widget = widgets.getWidget(id);
        // TODO check if widget id is the same
        container.unbindWidget();
        container.bindWidget(widget);
    }
    /**
     * @public
     */
    init() {
        this.root.init();
    }
}