/**
 * A simple html-element without any framework-attributes.
 */
class XISStaticElement {

    constructor(parent) {
        this.parent = parent;
        this.element = this.createElement();
        this.children = this.createChildren();
    }

    render() {
        this.parent.element.appendChild(this.element);
        this.children.forEach(child => child.render());
    }

    /**
     * @override
     * @param {any} childElement 
     */
    appendChild(childElement) {
        this.element.appendChild(childElement);
    }
    /**
     * Creates the DOM-Element.
     * 
     * @returns {any}
     */
    createElement() {
       // abstract     
    }

    /**
     * Creates the child-objects (not Dom-Elements)
     * @returns {Array}
     */
    createChildren() {
        // abstract
    }

}