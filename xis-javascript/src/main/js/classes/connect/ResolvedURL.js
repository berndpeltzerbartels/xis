
class ResolvedURL {

    /**
     * 
     * @param {Path} path
     * @param {Array{string: string}} pathVariables 
     * @param {{string: string}} urlParameters
     * @param {Page} page
     * @param {string} normalizedPath e.g. /x/*.html (* for path-variable)
     */
    constructor(path, pathVariables, urlParameters, page, normalizedPath) {
        this.path = path;
        this.pathVariables = pathVariables;
        this.urlParameters = urlParameters;
        this.page = page;
        this.normalizedPath = normalizedPath;
        this.url = this.toURL();
    }

    /**
    * Merges the array of path-variables an non-array object.
    * 
    * @public
    * @returns {{string: string}}
    */
    pathVariablesAsMap() {
        var map = {};
        for (var pathVariable of this.pathVariables) {
            var name = Object.keys(pathVariable)[0];
            var value = Object.values(pathVariable)[0];
            map[name] = value;
        }
        return map;
    }

    /**
     * @public
     * @returns {string}
     */
    toURL() {
        var url = '';
        var pathElement = this.path.pathElement;
        var pathVarIndex = 0;
        while (pathElement) {
            switch (pathElement.type) {
                case 'static': url += pathElement.content; break;
                case 'variable': {
                    var pathVar = this.pathVariables[pathVarIndex++];
                    url += Object.values(pathVar)[0];
                } break;
                default: throw new Error('unknown element-type: ' + pathElement.type);
            }
            pathElement = pathElement.next;
        }
        var urlParamNames = Object.keys(this.urlParameters);
        if (urlParamNames.length > 0) {
            urlParamNames.sort(); // for comparison
            url += '?';
            for (var i = 0; i < urlParamNames.length; i++) {
                var name = urlParamNames[i];
                url += name;
                url += '=';
                url += this.urlParameters[name]; // already encoded !
                if (i < urlParamNames.length - 1) {
                    url += '&';
                }
            }
        }
        return url;
    }

}