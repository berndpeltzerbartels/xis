class Includes {

    /**
     *
     * @param {HttpClient} client
     */
    constructor(client) {
        this.includes = {};
        this.client = client;
    }

    loadIncludes(config) {
        var _this = this;
        var promises = [];
        config.includeIds.forEach(key => _this.includes[key] = null);
        config.includeIds.forEach(key => promises.push(_this.loadInclude(key)));
        return Promise.all(promises).then(() => config);
    }

    /**
    * @returns {Promise<Include>}
    */
    loadInclude(key) {
        var _this = this;
        return this.client.loadInclude(key).then(includeHtml => {
            var include = new Include();
            include.key = key;
            include.html = includeHtml;
            _this.includes[key] = include;
        });
    }

    /**
     * @publicIn
     * @param {string} key
     * @returns {Include}
     */
    getInclude(key) {
        var include = this.includes[key];
        if (include === null || include === undefined) {
            throw new Error('no such include: ' + key);
        }
        return include;
    }

    /**
     * @public
     * @param {string} key
     * @returns {boolean}
     */
    hasInclude(key) {
        return this.includes[key] !== null && this.includes[key] !== undefined;
    }
}
