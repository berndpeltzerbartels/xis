/**
 * An HTML-element
 */
class XISElement extends XISTemplateObject {

    /**
     * @param {XISTemplateObject} parent 
     */
    constructor(parent) {
        super(parent, parent.getValueHolder());
    }

    init() {
        this.parent.element.appendChild(this.element);
        this.children.forEach(child => child.init());
    }

    /**
     * @override
     */
    refresh() {
        this.updateAttributes();
        this.children.forEach(child => child.refresh());
    }

    unlink() {
        this.parent.element.removeChild(this.element);
    }

    updateAttributes() {
        throw new Error('abstract method');
    }

    updateAttribute(name, value) {
        this.element.setAttribute(name, value);
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
     * @override
     * @returns {XISContainer}
     */
    getContainer() {
        return this.container;
    }


}