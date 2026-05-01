/**
 * @property {array<string>} pageIds
 * @property {array<string>} widgetIds
 * @property {string: PageAttributes} pageAttributes
 * @property {string: FrontletAttributes} widgetAttributes
 */

class ClientConfig {

    constructor() {
        this.pageIds = [];
        this.widgetIds = [];
        this.welcomePageId = undefined;
        this.pageAttributes = {}
        this.widgetAttributes = {};
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
     * @param {string} widgetId
     * @returns {string|undefined}
     */
    getWidgetHost(widgetId) {
        const attrs = this.widgetAttributes[widgetId];
        return attrs ? attrs.host : undefined;
    }
}
