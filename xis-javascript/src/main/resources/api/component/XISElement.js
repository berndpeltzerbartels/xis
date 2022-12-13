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
    init() {
        if (this.parent) { // null, if parent is a widget or a page
            var element = this.parent.getElement();
            if (element) {
                this.parent.getElement().appendChild(this.element);
            }
        }
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
        this.parent.getElement().removeChild(this.element);
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

    getElement() {
        return this.element;
    }

    /**
     * @override
     * @returns {XISContainer}
     */
    getContainer() {
        return this.container;
    }



}