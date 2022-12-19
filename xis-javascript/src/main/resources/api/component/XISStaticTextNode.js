/**
 * Static content of an element (html-tag) with no variables.
 */
class XISStaticTextNode extends XISTemplateObject {

    constructor(parent) {
        super(parent, parent.getValueHolder());
        this.className = 'XISStaticTextNode';
        this.node = this.createNode();
    }

    init() {
        this.parent.element.appendChild(this.node);

    }

    refresh() {
        // noop
    }

    createNode() {
        throw new Error('abstract method: createNode()');
    }

}