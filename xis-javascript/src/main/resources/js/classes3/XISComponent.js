class XISComponent extends XISValueHolder {

     /**
     *
     * @param {XISTemplateObject} parent
     */
    constructor(parent) {
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

}