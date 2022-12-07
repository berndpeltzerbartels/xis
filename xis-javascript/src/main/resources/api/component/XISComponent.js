class XISComponent extends XISValueHolder {

    /**
    * @param {XISClient} client
    * @param {XISTemplateObject} parent

    */
    constructor(client) {
        super(undefined);
        this.client = client;
        this.className = 'XISComponent';
        this.state = {};
    }

    /**
     * @public
     */
    init() {
        this.addPhaseMessage('init');
        super.init();
    }

    /**
     * @public
     */
    destroy() {
        this.addPhaseMessage('destroy');
        super.destroy();
    }

    /**
     * @public
     */
    show() {
        client.addMessage('show',);
        super.show();
    }

    /**
     * @public
     */
    hide() {
        this.addPhaseMessage('hide');
        super.hide();
    }

    /**
     * @public
     * @param {any} data 
     */
    processResponse(data) {
        this.addValues(data);
        this.refresh();
    }

    /**
     * @public
     * @param {String} action 
     */
    onAction(action) {
        this.client.addActionMessage(this, action, this.getActionData(action), this.getParameters());
    }

    /**
     * @protected
     * @param {String} action 
     * @returns {any}
     */
    getActionData(action) {
        var data = {};
        for (key in this.getActionStateKeys(action)) {
            data[key] = this.state[key];
        }
        return data;
    }

    /**
     * @private
     * @param {String} phase 
     */
    addPhaseMessage(phase) {
        this.client.addPhaseMessage(this, phase, this.getPhaseData(phase), this.getParameters());
    }

    /**
    * @private
    */
    getPhaseData(phase) {
        switch(phase) {
            case 'show': return this.get
        }
    }


    getParameters() {
        var parameters = {};
        for (key in this.getParameterNames()) {
            parameters[key] = parent.getValue(key);
        }
        return parameters
    }

    /**
     * @protected
     * @param {Array<String>} action 
     */
    getParameterNames() {
        throw new Error('abstract method');
    }


    /**
     * @protected
     * @param {String} action 
     */
    getActionStateKeys(action) {
        throw new Error('abstract method');
    }


    /**
     * @protected
     * @param {String} phase 
     */
    getOnShowStateKeys() {
        throw new Error('abstract method');
    }

    getOnDestroyStateKeys() {
     throw new Error('abstract method');
    }

    getOnHideStateKeys() {
     throw new Error('abstract method');
    }

    getOnInitStateKeys() {
     throw new Error('abstract method');
    }

    getOnDestroyStateKeys() {
     throw new Error('abstract method');
    }

    /**
     * @protected
     * @param {XISComponent} another 
     * @param {any} parameters 
     */
    replace(another, parameters) {
        throw new Error('abstract method');
    }

    getParameterNames() {
        // TODO
    }

}