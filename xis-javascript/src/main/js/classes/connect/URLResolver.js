class URLResolver {


    /**
     * 
     * @param {Pages} pages 
     */
    constructor(pages) {
        this.pages = pages;
    }

    /**
     * 
     * @param {string} url 
     * @returns {ResolvedURL} or false if the url does not match
     */
    resolve(url) {
        var index = url.indexOf('?');
        var pageUrl = index == -1 ? url : url.substring(0, index);
        for (var path of this.pages.getAllPaths()) {
            var pathVariables = path.evaluate(pageUrl);
            if (pathVariables) {
                var normalizedPath = path.normalized();
                var page = this.pages.getPage(normalizedPath);
                return new ResolvedURL(path, pathVariables, urlParameters(url), page);
            }

        }
        return false;
    }


}