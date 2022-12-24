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
        throw new Error('abstract method: createElement()');
    }

    bind() {
        this.parent.appendChildNode(this.element);
    }
    /**
     * @public
     * @override
     */
    init() {
        this.parent.appendChildNode(this.element);
        super.init();
    }

    appendChildNode(node) {
        this.element.appendChild(node);
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
        this.parent.removeChildNode(this.element);
    }

    updateAttributes() {
        throw new Error('abstract method: updateAttributes()');
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

    getNodes() {
        return [this.element];
    }

    /**
     * @override
     * @returns {XISContainer}
     */
    getContainer() {
        return this.container;
    }



}