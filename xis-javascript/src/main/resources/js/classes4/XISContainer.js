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
    * @param {XISTemplateObject} parent 
    * @param {XISValueHolder} valueHolder
    */
    init() {
        this.parent.element.appendChild(this.element);
       // TODO
    }

    refresh() {
        // TODO
    }

    /**
     * @override
     * @returns {XISContainer}
     */
    getContainer() {
        return this;
    }

    bindWidget(widget) {
        // TODO
        
    }

    unbindWidget() {
        
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
