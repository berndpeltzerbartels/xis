class IncludeInstance {

    /**
     * @param {string} key
     * @param {string} html
     */
    constructor(key, html) {
        this.key = key;
        this.rootElement = assertNotNull(normalizeElement(htmlToElement(html)));
        this.rootHandler = assertNotNull(initializeElement(this.rootElement));
    }
}
