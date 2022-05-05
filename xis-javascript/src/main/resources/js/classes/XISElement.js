/**
 * A html-element with framework-attributes.
 */
class XISElement extends XISTemplateElement {

    constructor(parent) {
        super(parent);
        this.element = this.createElement();
        this.children = this.createChildren();
        this.visible = false;
    }

    /**
     * @override
     */
    render() {
        if (!this.visible) {
            this.parent.element.appendChild(this.element);
            this.visible = true;
        }
        this.updateAttribues();
        this.children.forEach(child => child.render());

    }

    updateAttribues() {
        throw new Error('abstract method');
    }

    /**
     * @override
     * @param {*} childElement 
     */
    appendChild(childElement) {
        this.element.appendChild(childElement);
    }

    /**
     * @override
     * @param {*} childElement 
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

}