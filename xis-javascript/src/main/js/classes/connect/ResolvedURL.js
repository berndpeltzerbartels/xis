
class ResolvedURL {

    /**
     * 
     * @param {Path} path
     * @param {Array{string: string}} pathVariables 
     * @param {string: string} urlParameters
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
        var url = '';
        var pathElement = this.path.pathElement;
        while (pathElement) {
            switch (pathElement.type) {
                case 'static': url += pathElement.content; break;
                case 'variable': url += this.pathVariables[pathElement.key]; break;
                default: throw new Error('unknown element-type: ' + pathElement.type);
            }
            pathElement = pathElement.next;
        }
        return url;
    }


}