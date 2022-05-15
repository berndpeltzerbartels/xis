/**
 * An HTML-element
 */
class XISElement extends XISTemplateObject {

    /**
     * @param {XISTemplateObject} parent 
     * @param {XISValueHolder} valueHolder
     */
    constructor(parent) {
        super(parent);
        this.valueHolder = parent.getValueHolder();
        this.container = parent.getContainer();
        this.element = this.createElement();
        this.children = this.createChildren();
    }

    /**
     * @override
     */
    render() {
        this.parent.element.appendChild(this.element);
        this.updateAttribues();
        this.children.forEach(child => child.render());
    }

    unlink() {
        this.parent.getElement().removeChild(this.element);
    }

    updateAttribues() {
        throw new Error('abstract method');
    }

    /**
     * @override
     * @param {Node} childElement 
     */
    appendChild(childElement) {
        this.element.appendChild(childElement);
    }

    /**
     * @override
     * @param {Node} childElement 
     */
    removeChild(childElement) {
        this.element.removeChild(childElement);
    }

    /**
     * Creates the DOM-Element.
     * 
     * @override
     * @returns {any}
     */
    createElement() {
        throw new Error('abstract method');
    }

    /**
     * Creates the child-objects (not Dom-Elements)
     * 
     * @override
     * @returns {Array}
     */
    createChildren() {
        throw new Error('abstract method');
    }

    /**
     * @override
     * @returns {XISValueHolder}
     */
    getValueHolder() {
       return this.valueHolder;
    }

    /**
     * @override
     * @returns {XISContainer}
     */
    getContainer() {
        return this.container;
    }


}