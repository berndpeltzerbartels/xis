class XISWidget extends XISComponent {

    constructor(parent, client) {
        super(parent, client);
        this.className = 'XISWidget';
        this.componentType = 'WIDGET';

    }

        /**
         * @public
         */
        init() {
            this.root.init();
        }

    /**
     * @public
     * @override
     * @param {any} data 
     */
    addValues(data) {
        super.addValues(data);
        this.setValues(parent.getParameters());
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