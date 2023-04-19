
class Pages {
    /**
    * @param {Client} client
    * @param {Initializer} initializer
    */
    constructor(client, initializer) {
        this.client = client;
        this.initializer = initializer;
        this.pages = {};
    }

    /**
     *
     * @param {Config} config
     * @returns {Promise<any>}
     */
    loadPages(config) {
        console.log('Loading pages');
        this.welcomePageId = config.welcomePageId;
        this.pageAttributes = config.pageAttributes;
        var promises = [];
        var _this = this;
        config.pageIds.forEach(id => _this.pages[id] = new Page(id));
        config.pageIds.forEach(id => promises.push(_this.loadPageHead(id)));
        config.pageIds.forEach(id => promises.push(_this.loadPageBody(id)));
        config.pageIds.forEach(id => promises.push(_this.loadPageBodyAttributes(id)));
        return Promise.all(promises).then(() => config).catch(e => console.error(e));;
    }

    /**
     * @public
     * @param {String} uri
     * @returns {Page}
     */
    getPageById(uri) {
        return this.pages[uri];
    }

    getWelcomePage() {
        return this.pages[this.welcomePageId];
    }


    /**
     * @param {string} pageId
    * @returns {Promise<string>}
    */
    loadPageHead(pageId) {
        var _this = this;
        return this.client.loadPageHead(pageId).then(content => {
            var holder = document.createElement('div');
            holder.innerHTML = content;
            _this.pages[pageId].headChildArray = nodeListToArray(holder.childNodes);
            _this.pages[pageId].title =
                _this.initializer.initialize(holder);
            return pageId;
        });
    }

    /**
    * @private
    * @param {string} pageId
    * @returns {Promise<string>}
    */
    loadPageBody(pageId) {
        var _this = this;
        return this.client.loadPageBody(pageId).then(content => {
            var holder = document.createElement('div');
            holder.innerHTML = content;
            _this.pages[pageId].bodyChildArray = nodeListToArray(holder.childNodes);
            _this.initializer.initialize(holder);
            return pageId;
        });
    }

    /**
     * @private
     * @param {String} pageId
     * @returns
     */
    loadPageBodyAttributes(pageId) {
        var _this = this;
        return this.client.loadPageBodyAttributes(pageId).then(attributes => {
            _this.pages[pageId].bodyAttributes = attributes;
            return pageId;
        });
    }
}
