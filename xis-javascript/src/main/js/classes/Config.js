
/**
 * @property {array<string>} pageIds
 * @property {array<string>} widgetIds
 * @property {string: PageAttributes} pageAttributes
 * @property {string: WidgetAttributes} widgetAttributes
 */

class ClientConfig {

    constructor() {
        this.pageIds = [];
        this.widgetIds = [];
        this.welcomePageId = undefined;
        this.pageAttributes = {}
        this.widgetAttributes = {};
    }

    /**
     * @public
     * @param {string} id
     * @returns {string}
     */
    getPageHost(id) {
        return this.pageHosts[id];
    }

    /**
     * @public
     * @param {string} id
     * @returns {string}
     */
    getWidgetHost(id) {
        return this.widgetHosts[id];
    }
}
