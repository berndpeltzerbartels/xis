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
        this.subscribeToActions();
    }

    /**
    * @public
    */
    init() {
        var component = this;
        this.handlePhase('init', component.initTree);
    }


    /**
     * @public
     */
    destroy() {
        var component = this;
        this.handlePhase('destroy', component.destroyTree);
    }


    /**
     * @public
     */
    show() {
        var component = this;
        this.handlePhase('destroy', component.showTree);
    }


    /**
     * @public
     */
    hide() {
        var component = this;
        this.handlePhase('hide', component.hideTree);
    }


    handlePhase(phase, treeInvoker) {
        if (this.isActivePhase(phase)) {
            var component = this;
            var data = this.getPhaseData(phase);
            this.sendPhaseMessage(phase, data, treeInvoker);
        } else {
            treeInvoker();
        }
    }

    /**
     * @private
     * @param {String} phase 
     * @returns 
     */
    getPhaseData(phase) {
        var keys = [];
        switch (phase) {
            case 'init': keys = this.getOnInitStateKeys();
                break;
            case 'destroy': keys = this.getOnDestroyStateKeys();
                break;
            case 'show': keys = this.getOnShowStateKeys();
                break;
            case 'hide': keys = this.getOnHideStateKeys();
                break;
        }
        return this.getComponentData(keys);
    }


    /**
     * @public
     * @param {String} action 
     */
    onAction(action) {
        if (this.isActiveAction(action)) {
            var component = this;
            this.client.sendActionMessage(this, action, this.getActionData(action), this.getParameters(), () => component.refeshTree());
        }
    }

    subscribeToActions() {
        var component = this;
        for (var action of this.getActiveActions()) {
            this.getActionStateKeys.subscribe(action, (action) => component.onAction(action));
        }
    }

    /**
     * @private
     */
    initTree() {
        this.getChildren().forach(child => {
            child.init();
            child.refresh();
        });
    }


    /**
     * @private
     */
    destroyTree() {
        this.getChildren().forach(child => {
            child.destroy();
            child.refresh();
        });
    }

    /**
     * @private
     */
    showTree() {
        this.getChildren().forach(child => {
            child.show();
            child.refresh();
        });
    }

    /**
    * @private
    */
    hideTree() {
        this.getChildren().forach(child => {
            child.hide();
            child.refresh();
        });
    }

    refeshTree() {
        this.getChildren().forach(child => {
            child.refresh();
        });
    }

    /**
    * @protected
    */
    isActivePhase(phase) {
        throw new Error('abstract method: isActivePhase(phase)');
    }


    /**
    * @protected
    */
    isActiveAction(action) {
        return this.getActiveActions().indexOf(action) != -1;
    }


    /**
     * 
     * @param {String} phase 
     * @param {any} data 
     * @param {function callback() {
        
     }} callback 
     */
    sendPhaseMessage(phase, data, callback) {
        var component = this;
        this.client.sendPhaseMessage(phase, this, data, this.getParameters(), response => {
            component.addValues(response.data);
            callback();
        });
    }

    /**
     * @private
     * @param {String} keys 
     * @returns {any}
     */
    getComponentData(keys) {
        var data = {};
        for (var key of keys) {
            data[key] = this.getComponentValue(key);
        }
        return data;
    }

    /**
     * @private
     * @param {String} key
     * @returns {any}
     */
    getComponentValue(key) {
        return this.state[key];
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
     * @returns {any}
     */
    getParameters() {
        var parameters = {};
        for (var key in this.getParameterNames()) {
            parameters[key] = parent.getValue(key);
        }
        return parameters
    }

    /**
     * @protected
     * @param {Array<String>} action 
     */
    getParameterNames() {
        // TODO
        // throw new Error('abstract method');
        return [];
    }


    /**
     * @protected
     * @param {String} action 
     */
    getActionStateKeys(action) {
        throw new Error('abstract method: getActionStateKeys(action)');
    }

    /**
     * @protected
     * @returns {Array<String>}
     */
    getActiveActions() {
        throw new Error('abstract method: getActiveActions()');
    }

    getActivePhases() {
        throw new Error('abstract method: getActivePhases()');
    }

    /**
     * @protected
     * @param {String} phase 
     */
    getOnShowStateKeys() {
        throw new Error('abstract method: getOnShowStateKeys()');
    }

    getOnDestroyStateKeys() {
        throw new Error('abstract method: getOnDestroyStateKeys()');
    }

    getOnHideStateKeys() {
        throw new Error('abstract method: getOnHideStateKeys()');
    }

    getOnInitStateKeys() {
        throw new Error('abstract method: getOnInitStateKeys()');
    }

    getOnDestroyStateKeys() {
        throw new Error('abstract method: getOnDestroyStateKeys()');
    }

    bind(parent) {
        throw new Error('abstract method: bind()');
    }

    /**
     * @protected
     * @param {XISComponent} another 
     * @param {any} parameters 
     */
    replace(another, parameters) {
        throw new Error('abstract method:  replace(another, parameters)');
    }

}