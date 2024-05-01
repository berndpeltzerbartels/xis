
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
     * @param {ClientConfig} config
     * @returns {Promise<any>}
     */
    loadPages(config) {
        this.config = config;
        var promises = [];
        var _this = this;
        config.pageIds.forEach(id => _this.pages[id] = new Page(id, config.pageAttributes[id]));
        config.pageIds.forEach(id => promises.push(_this.loadPageHead(id)));
        config.pageIds.forEach(id => promises.push(_this.loadPageBody(id)));
        config.pageIds.forEach(id => promises.push(_this.loadPageBodyAttributes(id)));
        return Promise.all(promises).then(() => config).catch(e => console.error(e));
    }

    /**
     * @public
     * @returns {Page}
     */
    getWelcomePage() {
        return this.getPage(this.config.welcomePageId);
    }

    /**
     * @public
     * @returns {Array<Path>}
     */
    getAllPaths() {
        return Object.values(this.pages).map(page => page.pageAttributes.path);
    }

    /**
     * @public
     * @param {string} normalizedPath a path with path-variables represented by an asterisk
     */
    getPage(normalizedPath) {
        return this.pages[normalizedPath];
    }

    /**
    * @private
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
            var page = _this.pages[pageId];
            if (titleElement) {
                page.titleExpression = new TextContentParser(titleElement.innerText).parse();
            }
            page.headTemplate = templateElement;
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
            var page = _this.pages[pageId];
            page.bodyTemplate = templateElement;
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
