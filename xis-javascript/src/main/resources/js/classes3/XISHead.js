class XISHead extends XISElement {

    refresh() {
        this.updateAttributes();
        this.children.forEach(child => child.refresh());
    }
}