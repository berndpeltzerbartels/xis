
class Pages {
    /**
    * @param {Client} client
    */
    constructor(client) {
        this.client = client;
        this.pages = {};
    }

    /**
     *
     * @param {Config} config
     * @returns {Promise<any>}
     */
    loadPages(config) {
        this.config = config;
        var promises = [];
        var _this = this;
        config.pageIds.forEach(id => _this.pages[id] = new Page(id, new PageAttributes(config.pageAttributes[id])));
        config.pageIds.forEach(id => promises.push(_this.loadPageHead(id)));
        config.pageIds.forEach(id => promises.push(_this.loadPageBody(id)));
        config.pageIds.forEach(id => promises.push(_this.loadPageBodyAttributes(id)));
        return Promise.all(promises)
            .then(() => _this.paths = Object.values(this.pages).map(page => page.pageAttributes.path))
            .then(() => config).catch(e => console.error(e));
    }

    /**
     * @public
     * @returns {Page}
     */
    getWelcomePage() {
        return this.pages[this.config.welcomePageId];
    }

    /**
     * @public
     * @returns {Array<Path>}
     */
    getAllPaths() {
        return this.paths;
    }

    /**
     * @public
     * @param {string} normalizedPath a path with path-variables represented by an asterisk
     */
    getPage(normalizedPath) {
        return this.pages[normalizedPath];
    }
    /**
     * @param {string} pageId
    * @returns {Promise<string>}
    */
    loadPageHead(pageId) {
        var _this = this;
        return this.client.loadPageHead(pageId).then(content => {
            var templateElement = htmlToElement(content);
            initializeElement(templateElement);
            var headChildArray = nodeListToArray(templateElement.childNodes);
            var titleElement = headChildArray.find(child => isElement(child) && child.localName == 'title');
            if (titleElement) {
                _this.pages[pageId].titleExpression = new TextContentParser(titleElement.innerText).parse();
            }
            _this.pages[pageId].headChildArray = headChildArray;
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
            var templateElement = htmlToElement(content);
            initializeElement(templateElement);
            _this.pages[pageId].bodyChildArray = nodeListToArray(templateElement.childNodes);
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

    /**
     * for testing
     */
    reset() {
        this.pages = {};
    }
}
