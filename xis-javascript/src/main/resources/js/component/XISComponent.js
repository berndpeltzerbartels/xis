class XISComponent extends XISValueHolder {

    /**
    *
    * @param {XISTemplateObject} parent
    * @param {XISClient} client
    */
    constructor(parent, client) {
        super(parent);
        this.client = client;
        this.className = 'XISComponent';
        this.parent = parent;
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
     * 
     * @param {any} phase 
     * @returns {any}
     */
    getPhaseData(phase) {
        var data = {};
        for (key in this.getPhaseStateKeys(phase)) {
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
    getPhaseStateKeys(phase) {
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

}