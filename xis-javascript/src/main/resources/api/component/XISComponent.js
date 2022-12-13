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
        if (this.isActivePhase('init')) {
            var component = this;
            var data = this.getComponentData(this.getOnInitStateKeys());
            this.sendPhaseMessage('init', data, () => component.initTree());
        } else {
            this.initTree();
        }
    }


    /**
     * @public
     */
    destroy() {
        if (this.isActivePhase('destroy')) {
            var component = this;
            var data = this.getComponentData(this.getOnDestroyStateKeys());
            this.sendPhaseMessage('destroy', data, () => component.destroyTree());
        } else {
            this.destroyTree();
        }
    }


    /**
     * @public
     */
    show() {
        if (this.isActivePhase('show')) {
            var component = this;
            var data = this.getComponentData(this.getOnShowStateKeys());
            this.sendPhaseMessage('show', data, () => component.showTree());
        } else {
            this.showTree();
        }
    }


    /**
     * @public
     */
    hide() {
        if (this.isActivePhase('hide')) {
            var component = this;
            var data = this.getComponentData(this.getOnHideStateKeys());
            this.sendPhaseMessage('hide', data, () => component.hideTree());
        } else {
            this.hideTree();
        }
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
        throw new Error('abstract method');
    }


    /**
    * @protected
    */
    isActiveAction(phase) {
        throw new Error('abstract method');
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
        for (key of keys) {
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
        // TODO
        // throw new Error('abstract method');
        return [];
    }


    /**
     * @protected
     * @param {String} action 
     */
    getActionStateKeys(action) {
        throw new Error('abstract method');
    }

    getActiveActions() {
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

    bind(parent) {
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