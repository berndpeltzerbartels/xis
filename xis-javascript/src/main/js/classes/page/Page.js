/**
 * @typedef Page
 * @property {string} normalizedPath
 * @property {string} title
 * @property {Element} headTemplate
 * @property {Element} bodyTemplate
 * @property {{string: string}} bodyAttributes
 * @property {PageAttributes} pageAttributes
 * @property {TextContent} titleExpression
 * @property {Data} data
 * @property {RootTagHandler} rootTagHandler;
 */

class Page {

    /**
     * 
     * @param {string} normalizedPath 
     * @param {PageAttributes} pageAttributes 
     */
    constructor(normalizedPath, pageAttributes) {
        this.normalizedPath = normalizedPath;
        this.pageAttributes = pageAttributes;
        this.titleExpression = undefined;
        this.headTemplate = undefined;
        this.bodyTemplate = undefined;
        this.bodyAttributes = {};
        this.data = new Data({});
    }

}
