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
        if (index != -1) {
            url = url.substring(index);
        }
        for (var path of this.pages.getAllPaths()) {
            var pathVariables = path.evaluate(url);
            if (pathVariables) {
                var normalizedPath = path.normalized();
                var page = this.pages.getPage(normalizedPath);
                return new ResolvedURL(path, pathVariables, this.urlParameters(url), page);
            }

        }
        return false;
    }

    /**
   * @private
   * @param {string} url 
   * @returns 
   */
    urlParameters(url) {
        var urlParameters = {};
        var start = url.indexOf('?');
        if (start != -1) {
            var query = url.substring(start + 1);
            for (var keyValue of doSplit(query, '&')) {
                var param = doSplit(keyValue, '=');
                urlParameters[param[0]] = param[1];
            }
        }
        return urlParameters;
    }
}