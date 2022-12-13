class XISWidget extends XISComponent {

    constructor(client) {
        super(client);
        this.className = 'XISWidget';
        this.componentType = 'WIDGET';
    }

    getRoot() {
        return this.root; // used in generated code
    }

    bind(container) {
        container.getElement().appendChild(this.root.element);
        this.root.show();
    }

    unbind(container) {
        container.getElement().removeChild(this.root.element);
        this.root.hide();
    }

    /**
     * @public
     * @param {string} id 
     */
    replace(id) {
        var container = this.parent;
        var widget = widgets.getWidget(id);
        // TODO check if widget id is the same
        container.unbindWidget();
        container.bindWidget(widget);
    }

}