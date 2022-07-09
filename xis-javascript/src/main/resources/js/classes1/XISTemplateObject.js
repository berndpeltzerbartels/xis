class XISTemplateObject {

      /**
     * @param {XISTemplateObject} parent 
     */
    constructor(parent) {
        this.parent = parent;
    }

    onDataChanged() {

    }

    getValueHolder() {
        throw new Error('abstract method');
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

    getValueHolder() {
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