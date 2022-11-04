class XISWidget extends XISComponent {

    constructor() {
        super(undefined);
        this.type = 'widget';

    }

    replace(id) {
        var container = this.parent;
        var widget = widgets.getWidget(id);
        // TOD check if widget id is the same
        container.unbindWidget();
        container.bindWidget(widget);
    }
    /**
     * @public
     */
    init() {
        this.root.init(this.element, this);
        this.childNodes = nodeListToArray(this.element.childNodes);
    }
}