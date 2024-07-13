/**
 * @typedef Path
 * @property {PathElement} pathElement the first one of linked elements
 * @property pageId
 */
class Path {

    /**
     * 
     * @param {PathElement} pathElement 
     */
    constructor(pathElement) {
        this.pathElement = pathElement;
    }

    isStatic() {
        return this.pathElement.next == undefined && this.pathElement.type == 'static';
    }


    /**
     * Evaluates the path-variables as an array
     * of key-values pairs in original order they 
     * are placed in url.
     * 
     * @public
     * @param {string} realPath 
     * @returns {Array<string: string>} pathVariables in case path is matching, otherwise false
     */
    evaluate(realPath) {
        if (this.pathElement) {
            var pathVariables = [];
            if (this.pathElement.evaluate(realPath, pathVariables)) {
                return pathVariables;
            }
        }
        return false;
    }

    /**
     * Creates a path as an array with path-variables
     * represented by an asterisk.
     * 
     * @public
     * @returns {string}
     */
    normalized() {
        if (this.pathElement) {
            return this.pathElement.appendNormalized();
        }
        return '';
    }
}