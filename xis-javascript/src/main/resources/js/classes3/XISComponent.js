class XISComponent extends XISValueHolder {

    /**
    *
    * @param {XISTemplateObject} parent
    */
    constructor(parent) {
        super(parent);
        this.className = 'XISComponent';
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

    init() {
        this.callServer('init');
        super.init();
    }

    destroy() {
        this.callServer('destroy');
        super.destroy();
    }

    show() {
        this.callServer('show');
        super.show();
    }

    hide() {
        this.callServer('hide');
        super.hide();
    }

    callServer(phase) {
        var dataOut = {};
        var component = this;
        if (this.getKeysOut(phase) || this.getKeysIn(phase)) {
            this.getKeysOut(phase).forEach(key => {
                dataOut[key] = component.getValue(key);
            });
            var dataIn = {};
            client.send(phase, dataOut, response => {
                this.getKeysIn(phase).forEach(key => {
                    dataIn[key] = response[key];
                });
                if (response.componentId) {
                    component.replace(response.componentId);
                }
            });
            this.setValues(dataIn);
        }
    }

    /**
     * @private
     * @param {String} phase
     * @returns {Array}
     */
    getKeysIn(phase) {
        throw new Error('abstract method');
    }

    /**
     * @private
     * @param {String} phase
     * @returns {Array}
     */
    getKeysOut(phase) {
        throw new Error('abstract method');
    }


}