class XISWidget extends XISVisible {

    constructor() {
        super(undefined);

    }

    /**
     * @public
     */
    init() {
        this.root.init(this.element, this);
        this.childNodes = nodeListToArray(this.element.childNodes);
    }
}