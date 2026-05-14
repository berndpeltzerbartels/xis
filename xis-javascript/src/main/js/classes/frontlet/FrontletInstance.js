class FrontletInstance {

    /**
     * @param {Frontlet} frontlet
     * @param {Frontlets} frontlets
     */
    constructor(frontlet, frontlets) {
        this.frontlet = frontlet;
        this.frontlets = frontlets;
        this.root = assertNotNull(normalizeElement(htmlToElement(frontlet.html)));
        this.rootHandler = assertNotNull(initializeElement(this.root));
    }

    dispose() {
        this.frontlets.disposeInstance(this);
    }


}
