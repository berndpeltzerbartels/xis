/**
 * Element for displaying widgets
 */
class XISContainer extends XISTemplateObject {

    /**
    * @param {XISTemplateObject} parent
    */
    constructor(parent) {
        super(parent, parent.getValueHolder());
    }

    /**
     * @public
     */
    init() {
        if (this.defaultWidgetId) {
            var widget = widgets.getWidget(this.defaultWidgetId);
            this.bindWidget(widget);     
        }
      //  this.parent.element.appendChild(this.element);
       // TODO
    }

    refresh() {
        this.updateAttributes();
        if (this.widget) {
            this.widget.refresh();
        }
    }

    /**
     * @override
     * @returns {XISContainer}
     */
    getContainer() {
        return this;
    }

    bindWidget(widget) {
        this.widget = widget;
        this.element.appendChild(widget.element);
        this.widget.refresh();    
    }

    unbindWidget() {
        if (this.widget) {
            this.element.removeChild(this.widget.element);    
        }     
    }

    update() {
        this.updateAttributes();
        if (this.widget) {
            this.widget.update();
        }
    }

    updateAttributes() {
        throw new Error('updateAttributes is abstract in ' + this);
    }

    updateAttribute(name, value) {
        this.element.setAttribute(name, value);
    }

    unlink() {
        this.parent.removeChild(this.element);
    }

    /**
     * @override
     * @param {Node} element 
     */
    removeChild(element) {
        this.element.removeChild(element);
    }

}
