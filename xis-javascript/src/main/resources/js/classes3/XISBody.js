class XISBody extends XISElement {

    bind(parent) {
        // noop
    }

    refresh() {
        this.updateAttributes();
        this.children.forEach(child => child.refresh());
    }
}