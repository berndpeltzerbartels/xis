class XISTemplateObject {


    /**
     * 
     * @param {XISTemplateObject} parent 
     */
    constructor(parent) {
        this.className = 'XISTemplateObject';
        this.parent = parent;
    }

    init() {
        this.children.forEach(child.init());
    }

    destroy() {
        this.children.forEach(child.destroy());
    }

    show() {
        this.children.forEach(child.show());
    }

    hide() {
        this.children.forEach(child.hide());
    }

    // TODE: Kann refresh wegen show ganz verschwinden ?
    refresh() {
        this.children.forEach(child.refresh());
    }

    /**
     * @returns {XISValueHolder}
     */
    getValueHolder() {
        return this.parent.getValueHolder();
    }

    /**
     * @returns {Node}
     */
    getElement() {
        return this.parent.getELement();
    }


    getValue(key) {
        return this.getValueHolder().getValue(key);
    }

    getChildren() {
        return this.children;
    }


}