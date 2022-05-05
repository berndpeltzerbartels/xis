class XISTemplateElement {

    constructor(parent, valueHolder) {
        this.parent = parent;
        this.valueHolder = valueHolder;
    }

    createElement() {
        throw new Error('abstract method');
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