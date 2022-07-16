class XISWidget extends XISValueHolder{

    constructor() {
        super();
        this.valueHolder = { getValue: function(path){return undefined;}};
    }

    init() {
        this.root.init(this.element, this);
        this.childNodes = nodeListToArray(this.element.childNodes);
    }
}