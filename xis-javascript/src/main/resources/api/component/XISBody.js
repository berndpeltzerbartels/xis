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
    init() {
        // Do not bind to parent, here
        this.children.forEach(child => child.init());
    }

}