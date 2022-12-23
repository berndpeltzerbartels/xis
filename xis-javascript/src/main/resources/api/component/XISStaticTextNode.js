/**
 * Static content of an element (html-tag) with no variables.
 */
class XISStaticTextNode extends XISTemplateObject {

    constructor(parent) {
        super(parent, parent.getValueHolder());
        this.className = 'XISStaticTextNode';
        this.node = textNode();
    }

    refresh() {
        // noop
    }

    createNode() {
        throw new Error('abstract method: createNode()');
    }

    init() {
        this.bind();
    }

    bind() {
        this.parent.getElement().appendChild(this.node);
    }

    destroy() {

    }

    show() {

    }

    hide() {

    }


}