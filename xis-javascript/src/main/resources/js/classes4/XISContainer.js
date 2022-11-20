/**
 * Element for displaying widgets
 */
class XISContainer extends XISTemplateObject {

    /**
    * @param {XISTemplateObject} parent
    */
    constructor(parent) {
        super(parent);
        this.className = 'XISContainer';
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
    }

    /**
    * @public
    * @override
    */
    destroy() {
        if (this.widget) {
            this.widget.destroy();
        }
    }


    /**
     * @public
     * @override
     */
    show() {
        if (this.widget) {
            this.widget.show();
        }
    }

    /**
     * @public
     * @override
     */
    hide() {
        if (this.widget) {
            this.widget.hide();
        }
    }


    /**
     * TODO is this still used somewhere ?
     * @override
     * @returns {XISContainer}
     */
    getContainer() {
        return this;
    }

    bindWidget(widget) {
        if (this.widget && this.widget != widget) {
            this.widget = widget;
            if (widget.root) {
                this.getParentElement().appendChild(widget.root.element);
            }
            this.widget.show();
        }
    }

    unbindWidget() {
        if (this.widget) {
            this.getParentElement().removeChild(this.widget.root.element);
        }
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
