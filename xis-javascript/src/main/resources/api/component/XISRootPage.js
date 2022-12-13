class XISRootPage {

    /**
     * 
     * @param {XISClient} client 
     */
    constructor(client) {
        this.className = 'XISRootPage';
        this.client = client;

    }

    /**
     * @public
     */
    onMainPageLoaded() {
        this.head = getElementByTagName('head');
        this.body = getElementByTagName('body');
        this.title = getChildByName(this.head, 'title');
    }

    /**
     * @public
     */
    onMainPageUnloaded() {
        if (this.page) {
            this.page.hide();
            this.page.destroy();
            this.page = undefined;

        }
    }

    /**
     * Called from main.html
     * @public
     * @param {XISPage} page 
     */
    bindPage(page) {
        this.page = page;
        this.page.bind(this);
        this.page.show();
        this.page.refresh();
    }

    /**
     * @public
     */
    unbindPage() {
        if (this.page) {
            this.page.unbind();
            this.page.hide();
            this.page = undefined;
        }
    }
}