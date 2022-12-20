/**
 * Element for displaying widgets
 */
class XISContainer extends XISElement {

    /**
    * @param {XISTemplateObject} parent
    */
    constructor(parent) {
        super(parent);
        this.className = 'XISContainer';
        this.children = [];
    }

    /**
     * @public
     * @override
     */
    init() {
        if (this.defaultWidgetId) {
            var widget = widgets.getWidget(this.defaultWidgetId);
            this.bindWidget(widget);
        }
        super.init();
    }

    /**
    * @public
    * @override
    */
    destroy() {
        if (this.widget) {
            this.widget.destroy();
        }
        super.destroy();
    }


    /**
     * @public
     * @override
     */
    show() {
        if (this.widget) {
            this.widget.show();
        }
        super.show();
    }

    /**
     * @public
     * @override
     */
    hide() {
        if (this.widget) {
            this.widget.hide();
        }
        super.hide();
    }


    /**
     * TODO is this still used somewhere ?
     * @override
     * @returns {XISContainer}
     */
    getContainer() {
        return this;
    }


    /**
     * @public
     * @param {XISWidget} widget 
     */
    bindWidget(widget) {
        this.children.forEach(child => child.unlink()); // remove placeholder
        if (this.widget && this.widget != widget) {
            this.widget = widget;
            this.widget.bind(this);
            this.widget.show();
            this.children = [this.widget];
        }
    }

    /**
    * @public
    * @param {XISWidget} widget 
    */
    unbindWidget() {
        if (this.widget) {
            this.parent.getElement().removeChild(this.widget.root.element);
            this.widget.hide();
            this.widget.setParent(undefined);
        }
        this.children.forEach(child => child.bind()); // restore placeholder
    }

    update() {
        this.updateAttributes();
        if (this.widget) {
            this.widget.update();
        }
    }

    unlink() {
        this.parent.removeChild(this.element);
    }

}
