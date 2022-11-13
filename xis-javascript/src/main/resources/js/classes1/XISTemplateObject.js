class XISTemplateObject {


    /**
     * 
     * @param {XISTemplateObject} parent 
     */
    constructor(parent) {
        this.className = 'XISTemplateObject';
        this.parent = parent;
    }

    refresh() {
        throw new Error('abstract method');
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