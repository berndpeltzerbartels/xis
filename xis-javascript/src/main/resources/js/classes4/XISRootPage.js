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

    onMainPageUnloaded() {
        if (this.page) {
            this.page.hide();
            this.page.destroy(); // TODO Check if method is the same to avoid duplicate invocation on server-side
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