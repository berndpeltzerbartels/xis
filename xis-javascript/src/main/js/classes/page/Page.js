/**
 * @typedef Page
 * @property {string} normalizedPath
 * @property {string} title
 * @property {array<Element>} headChildArray
 * @property {array<Element>} bodyChildArray
 * @property {{string: string}} bodyAttributes
 * @property {PageAttributes} pageAttributes
 * @property {TextContent} titleExpression
 * @property {Data} data
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
        this.headChildArray = [];
        this.bodyChildArray = [];
        this.bodyAttributes = {};
        this.data = new Data({});
    }

}
