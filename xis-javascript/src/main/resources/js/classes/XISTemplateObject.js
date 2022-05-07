class XISTemplateObject {

      /**
     * @param {XISTemplateObject} parent 
     * @param {XISValueHolder} valueHolder
     */
    init(parent, valueHolder) {
        this.parent = parent;
        this.valueHolder = valueHolder;
    }

    createChildren() {
        throw new Error('abstract method');
    }


    render() {
        throw new Error('abstract method');
    }

    getValue(path) {
        return this.valueHolder.getValue(path);
    }

    appendChild(childElement) {
        throw new Error('abstract method');
    }

    removeChild(childElement) {
        throw new Error('abstract method');
    }

}