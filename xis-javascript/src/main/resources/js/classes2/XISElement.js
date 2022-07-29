/**
 * An HTML-element
 */
class XISElement extends XISTemplateObject {

    /**
     * @param {XISTemplateObject} parent 
     */
    constructor(parent) {
        super(parent);
        this.valueHolder = parent.getValueHolder();
    }

    /**
     * @override
     */
    refresh() {
        debugger;
        // TODO. Das ist Schrott. Da es im Konstruktor das Problem mit der Reihenfolge gibt, entweder das lösen, oder bind rekursiv separat aufrufen.
        if (this.requiresBinding()) {
            this.bind();
        }
        this.updateAttributes();
        this.children.forEach(child => child.refresh());
    }

    requiresBinding() {
        return !this.parentElement || this.parentElement != this.parent.element;
    }

    bind() {
        this.parent.element.appendChild(this.element);
        this.parentElement = this.parent.element;
    }

    unlink() {
        this.parent.element.removeChild(this.element);
    }

    updateAttributes() {
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