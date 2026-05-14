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

    loadFrontlets(config) {
        var _this = this;
        var promises = [];
        this.frontletAttributes = config.frontletAttributes;
        config.frontletIds.forEach(id => _this.frontlets[id] = {});
        config.frontletIds.forEach(id => promises.push(_this.loadFrontlet(id)));
        return Promise.all(promises).then(() => config);
    }
    /**
    * @returns {Promise<string>}
    */
    loadFrontlet(frontletId) {
        var _this = this;
        return this.client.loadFrontlet(frontletId).then(frontletHtml => {
            var frontlet = new Frontlet();
            frontlet.id = frontletId;
            frontlet.html = frontletHtml;
            frontlet.frontletAttributes = _this.frontletAttributes[frontletId];
            _this.addFrontlet(frontletId, frontlet);
        });
    }

    /**
     * @public
     * @param {string} frontletId
     * @param {Frontlet} frontlet
     */
    addFrontlet(frontletId, frontlet) {
        this.frontlets[frontletId] = frontlet;
    }

    /**
    * @public
    * @param {string} frontletId
    * @returns {FrontletInstance}
    */
    getFrontletInstance(frontletId) {
        if (!this.frontletInstances[frontletId]) {
            this.frontletInstances[frontletId] = [];
        }
        var instances = this.frontletInstances[frontletId];
        var frontletInstance = instances.shift();
        if (!frontletInstance) {
            var frontlet = this.frontlets[frontletId];
            if (!frontlet) {
                throw new Error('no such frontlet: ' + frontletId);
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
