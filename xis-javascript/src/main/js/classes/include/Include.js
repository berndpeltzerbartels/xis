/**
 * @class Include
 * @package classes/include
 * @access public
 * @description Represents a single include with its HTML content and root handler
 * 
 * @property {String} key
 * @property {String} html
 * @property {RootTagHandler} rootHandler
 */
class Include {
    constructor() {
        this.key = undefined;
        this.html = undefined;
        this.includeInstances = [];
    }

    fetchIncludeInstance() {
        if (this.includeInstances.length == 0) {
            return new IncludeInstance(this.key, this.html);
        }
        return this.includeInstances.pop();
    }

    releaseIncludeInstance(includeInstance) {
        this.includeInstances.push(includeInstance);
    }
}
