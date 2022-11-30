/**
 * An HTML-element
 */
class XISElement extends XISTemplateObject {

    /**
     * @param {XISTemplateObject} parent 
     */
    constructor(parent) {
        super(parent);
        this.className = 'XISElement';
    }

    /**
     * @public
     * @override
     */
    createElement() { // TODO add this to java-model
        throw new Error('abstract method');
    }

    /**
     * @public
     * @override
     */
    getParentElement() {
       return this.element;
    }

    /**
     * @public
     * @override
     */
    init() {
        this.element = this.createElement(); 
        this.parent.element.appendChild(this.element);
        super.init();
    }

    /**
     * @public
     * @override
     */
    refresh() {
        this.updateAttributes();
        super.refresh();
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