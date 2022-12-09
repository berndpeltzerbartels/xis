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
        return this.createElement('body'); // dummy to keep child-nodes
    }

    /**
     * @public
     * @override
     */
    getElement() {
        return this.element;
    }
}