class XISStaticTextNode extends XISTemplateObject {

    constructor(parent) {
        super(parent);
    }

    init() {
        debugger;
        this.parent.element.appendChild(this.node);

    }

    refresh() {
        // noop
    }

}