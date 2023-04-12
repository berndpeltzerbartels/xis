
/**
 * @property {array<string>} pageIds
 * @property {array<string>} widgetIds
 * @property {any} pageHosts
 * @property {any} widgetHosts
 * @property {any} pageAttributes
 * @property {any} widgetAttributes
 */
class Config {

    constructor() {
        this.pageIds = [];
        this.widgetIds = [];
        this.pageHosts = {};
        this.widgetHosts = {};
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


module.exports = {
	Config: Config
};
