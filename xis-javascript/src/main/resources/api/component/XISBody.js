class XISBody extends XISElement {

    /**
     * 
     * @param {XISPage} parentPage 
     */
    constructor(parentPage) {
        super(parentPage);
        this.className = 'XISBody';
    }

    /**
     * @public
     * @override
     */
    createElement() {
        return createElement('body'); // dummy to keep child-nodes
    }


    init() {
        this.children.forEach(child => child.init());
    }

    destroy() {
        this.children.forEach(child => child.destroy());
    }

    show() {
        this.children.forEach(child => child.show());
    }

    hide() {
        this.children.forEach(child => child.hide());
    }

}