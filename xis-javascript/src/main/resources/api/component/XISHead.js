class XISHead extends XISElement {

    constructor(parentPage) {
        super(parentPage);
        this.className = 'XISHead';
    }

    /**
    * @public
    * @override
    */
    init() {
        // Do not bind to parent, here
        this.children.forEach(child => child.init());
    }


}