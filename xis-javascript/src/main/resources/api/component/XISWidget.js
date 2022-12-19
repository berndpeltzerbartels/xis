class XISWidget extends XISComponent {

    constructor(client) {
        super(client);
        this.className = 'XISWidget';
        this.componentType = 'WIDGET';
        this.element = createElement('div');
    }

    getElement() {
        return this.element;
    }

    bind(container) {
        this.element.children.forEach(element => {
            container.getElement().appendChild(element);
        });
        super.show();
    }

    unbind(container) {
        this.element.children.forEach(element => {
            container.getElement().removeChild(element);
        });
        super.hide();
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