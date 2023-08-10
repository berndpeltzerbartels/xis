
class ResolvedURL {

    /**
     * 
     * @param {Path} path
     * @param {Array{string: string}} pathVariables 
     * @param {{string: string}} urlParameters
     * @param {Page} page
     */
    constructor(path, pathVariables, urlParameters, page) {
        this.path = path;
        this.pathVariables = pathVariables;
        this.urlParameters = urlParameters;
        this.page = page;
    }

    /**
     * @public
     * @returns {string}
     */
    toURL() {
        debugger;
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
            url += '?';
            for (var i = 0; i < urlParamNames.length; i++) {
                var name = url[i];
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