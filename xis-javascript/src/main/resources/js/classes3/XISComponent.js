class XISComponent extends XISValueHolder {

     /**
     *
     * @param {XISTemplateObject} parent
     */
    constructor(parent) {
        super(parent);
        this.parent = parent;
        this.state = {};
    }

    loadModel() {
        throw new Error('abstract method');
    }

    processData(data) {
        this.state = data;
        this.setValues(data);
        this.refresh();
    }


    getInitClientKeys() {
        throw new Error('abstract method');
    }

    replace(another) {
        throw new Error('abstract method');
    }

}