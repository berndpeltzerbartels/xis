class XISRootPage  {

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
    init() {
        this.head = getElementByTagName('head');
        this.body = getElementByTagName('body');
        this.title = getChildByName(this.head, 'title');
    }

    /**
     * Called from main.html
     * @public
     * @param {XISPage} page 
     */
    bindPage(page) {
        this.page = page;
        this.page.bind(this);
    }

    /**
     * @public
     */
    unbindPage() {
        if (this.page) {
            this.page.unbind();
            this.page = undefined;
        }
    }
}