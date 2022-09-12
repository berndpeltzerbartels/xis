/**
 * Static content of an element (html-tag) with no variables.
 */
class XISStaticTextNode extends XISTemplateObject {

    constructor(parent) {
        super(parent, parent.getValueHolder());
    }

    init() {
        this.parent.element.appendChild(this.node);

    }

    refresh() {
        // noop
    }

}