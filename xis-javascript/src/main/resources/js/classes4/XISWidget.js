class XISWidget extends XISVisible {

    constructor() {
        super(undefined);
        this.type = 'widget';

    }

    /**
     * @public
     */
    init() {
        this.root.init(this.element, this);
        this.childNodes = nodeListToArray(this.element.childNodes);
    }

    updateState(newState) {
    // TODO
    }
}