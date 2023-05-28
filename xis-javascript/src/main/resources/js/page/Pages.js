
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
            var shadowHead = htmlToElement(content);
            console.log('initialize head');
            initializeElement(shadowHead);
            var headChildArray = nodeListToArray(shadowHead.childNodes);
            var title = headChildArray.find(child => isElement(child) && child.localName == 'title');
            _this.pages[pageId].headChildArray = headChildArray;
            _this.pages[pageId].title = title ? title.innerText : '';

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
            var shadowBody = htmlToElement(content);
            initializeElement(shadowBody);
            _this.pages[pageId].bodyChildArray = nodeListToArray(shadowBody.childNodes);
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
