/**
 * @class IncludeHandler
 * @extends {TagHandler}
 * @package classes/handler
 * @access public
 * @description This handler is responsible for handling xis:include tags by loading and inserting the HTML content
 * of the registered include as child nodes.
 * 
 * @property {Includes} includes
 * @property {String} key
 */
class IncludeHandler extends TagHandler {

    /**
     *
     * @param {Element} tag
     * @param {Includes} includes
     * @param {Initializer} initializer
     */
    constructor(tag, includes, initializer) {
        super(tag);
        this.includes = includes;
        this.initializer = initializer;
        this.key = tag.getAttribute('name');
        this.type = 'include-handler';
        this.includeInstance = null;
    }

    /**
     * @public
     * @param {Data} data 
     * @returns {Promise}
     */
    refresh(data) {
        var include = this.includes.getInclude(this.key);
        if (this.includeInstance) {
            if (this.includeInstance.key === include.key) {
            // Already loaded
                return this.refreshDescendantHandlers(data);
            } else {
                // Release previous instance
                include.releaseIncludeInstance(this.includeInstance);
                this.descendantHandlers = [];
            }
        }
        this.includeInstance = include.fetchIncludeInstance();
        this.tag.appendChild(this.includeInstance.rootElement);
        this.addDescendantHandler(this.includeInstance.rootHandler);

        // After loading, refresh descendants
        return this.refreshDescendantHandlers(data);
    }

    /**
     * @public
     * @returns {boolean}
     */
    matches() {
        return true;
    }
}
