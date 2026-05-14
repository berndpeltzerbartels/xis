class URLResolver {


    /**
     * 
     * @param {Pages} pages 
     */
    constructor(pages) {
        this.pages = pages;
    }

    /**
     * @private
     * @param {Pages} pages 
     */
    init() {
        this.staticPathMap = {};
        this.dynamicPaths = [];
        this.pages.getAllPaths().forEach(path => {
            if (path.isStatic()) {
                this.staticPathMap[path.normalized()] = path;
            } else {
                this.dynamicPaths.push(path);
            }
        });
    }


    /**
     * 
     * @param {string} url 
     * @returns {ResolvedURL} or false if the url does not match
     */
    resolve(url) {
        if (url.indexOf("*") !== -1) {
            throw new Error('URL must not contain a wildcard (*)');
        }
        if (this.staticPathMap == undefined) {
            this.init(); // fails in constructor, because pages are not loaded yet
        }
        var index = url.indexOf('?');
        var pageUrl = index == -1 ? url : url.substring(0, index);
        if (this.staticPathMap[pageUrl]) {
            return new ResolvedURL(this.staticPathMap[pageUrl], [], urlParameters(url), this.pages.getPage(pageUrl), pageUrl);
        }
        for (var path of this.dynamicPaths) {
            var pathVariables = path.evaluate(pageUrl);
            if (pathVariables) {
                var normalizedPath = path.normalized();
                var page = this.pages.getPage(normalizedPath);
                return new ResolvedURL(path, pathVariables, urlParameters(url), page, normalizedPath);
            }
        }
        return false;
    }


}