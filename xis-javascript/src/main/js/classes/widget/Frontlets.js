class Frontlets {

    /**
     *
     * @param {HttpClient} client
     */
    constructor(client) {
        this.frontlets = {};
        this.frontletInstances = {};
        this.client = client;
        this.frontletAttributes = {};
    }

    loadWidgets(config) {
        var _this = this;
        var promises = [];
        this.frontletAttributes = config.widgetAttributes;
        config.widgetIds.forEach(id => _this.frontlets[id] = {});
        config.widgetIds.forEach(id => promises.push(_this.loadFrontlet(id)));
        return Promise.all(promises).then(() => config);
    }
    /**
    * @returns {Promise<string>}
    */
    loadFrontlet(widgetId) {
        var _this = this;
        return this.client.loadFrontlet(widgetId).then(widgetHtml => {
            var frontlet = new Frontlet();
            frontlet.id = widgetId;
            frontlet.html = widgetHtml;
            frontlet.frontletAttributes = _this.frontletAttributes[widgetId];
            _this.addFrontlet(widgetId, frontlet);
        });
    }

    /**
     * @public
     * @param {string} widgetId 
     * @param {Frontlet} frontlet
     */
    addFrontlet(widgetId, frontlet) {
        this.frontlets[widgetId] = frontlet;
    }

    /**
    * @public
    * @param {string} widgetId
    * @returns {FrontletInstance}
    */
    getFrontletInstance(widgetId) {
        if (!this.frontletInstances[widgetId]) {
            this.frontletInstances[widgetId] = [];
        }
        var instances = this.frontletInstances[widgetId];
        var frontletInstance = instances.shift();
        if (!frontletInstance) {
            var frontlet = this.frontlets[widgetId];
            if (!frontlet) {
                throw new Error('no such widget: ' + widgetId);
            }
            frontletInstance = new FrontletInstance(frontlet, this);
        }
        return frontletInstance;
    }

    /**
     * @public
     * @param {FrontletInstance} frontletInstance
     */
    disposeInstance(frontletInstance) {
        var instances = this.frontletInstances[frontletInstance.frontlet.id];
        frontletInstance.containerHandler = undefined;
        instances.push(frontletInstance);
    }

    reset() {
        this.frontlets = {};
        this.frontletInstances = {};
        this.frontletAttributes = {};
    }

}
