/**
 * @property {array<string>} pageIds
 * @property {array<string>} frontletIds
 * @property {string: PageAttributes} pageAttributes
 * @property {string: FrontletAttributes} frontletAttributes
 */

class ClientConfig {

    constructor() {
        this.pageIds = [];
        this.frontletIds = [];
        this.welcomePageId = undefined;
        this.pageAttributes = {}
        this.frontletAttributes = {};
        this.pendingEventTtlSeconds = 0;
    }

    /**
     * Returns the remote host for the given page (normalised path), or undefined if local.
     * @public
     * @param {string} normalizedPath
     * @returns {string|undefined}
     */
    getPageHost(normalizedPath) {
        const attrs = this.pageAttributes[normalizedPath];
        return attrs ? attrs.host : undefined;
    }

    /**
     * Returns the remote host for the given widget-id, or undefined if local.
     * @public
     * @param {string} frontletId
     * @returns {string|undefined}
     */
    getFrontletHost(frontletId) {
        const attrs = this.frontletAttributes[frontletId];
        return attrs ? attrs.host : undefined;
    }

    getWidgetHost(frontletId) {
        return this.getFrontletHost(frontletId);
    }
}
