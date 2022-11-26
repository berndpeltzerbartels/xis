class XISIf extends XISTemplateObject {

    /**
     * @param {XISTemplateObject} parent 
     */
    constructor(parent) {
        super(parent, parent.getValueHolder());
        this.className = 'XISIf';
        this.valueHolder = parent.getValueHolder();
        this.container = parent.getContainer();
        this.path = this.getPath();
        this.element = this.createElement();
        this.children = this.createChildren();
    }


    init() {
        // noop
    }


    /**
     * @override
     */
    render() {
        if (this.evaluateCondition()) {
            if (!this.children) {
                this.childern = this.createChildren();
            }
        } else {
            this.unlink();
        }
    }


    evaluateCondition() {
        throw new Error("abstract method");
    }

    getPath() {
        throw new Error("abstract method");
    }


    getElement() {
        return parent.getElement();
    }

    unlink() {
        for (child of this.children) {
            child.unlink();
        }
    }

    /**
     * @override
     * @returns {XISContainer}
     */
    getContainer() {
        return this.container;
    }

}