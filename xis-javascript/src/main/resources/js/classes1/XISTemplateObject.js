class XISTemplateObject {


    /**
     * 
     * @param {XISTemplateObject} parent 
     */
    constructor(parent) {
        this.className = 'XISTemplateObject';
        this.parent = parent;
    }

    getParentElement() {
        if (this.parent) {
            return this.parent.getParentElement()
        }
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

    refresh() {
        this.children.forEach(child.refresh());
    }

    /**
     * @returns {XISValueHolder}
     */
    getValueHolder() {
        return this.parent.getValueHolder();
    }


    val(path) {
        return this.getValueHolder().getValue(path);
    }

    getChildren() {
        return this.children;
    }

    onDataChanged() {

    }

    getContainer() {
        throw new Error('abstract method');
    }

    createChildren() {
        throw new Error('abstract method');
    }

    render() {
        throw new Error('abstract method');
    }

    appendChild(childElement) {
        throw new Error('abstract method');
    }

    removeChild(childElement) {
        throw new Error('abstract method');
    }

    getElement() {
        throw new Error('abstract method');
    }

    unlink() {
        throw new Error('abstract method');
    }


}