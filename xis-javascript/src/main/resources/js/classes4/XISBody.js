class XISBody extends XISElement {

    constructor(parent) {
        super(parent);
        this.className = 'XISBody';
    }

    init() {
        this.children.forEach(child => child.init());
    }

    refresh() {
        this.updateAttributes();
        this.children.forEach(child => child.refresh());
    }
}