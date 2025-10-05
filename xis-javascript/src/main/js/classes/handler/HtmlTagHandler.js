class HtmlTagHandler extends TagHandler {

    constructor(tagHandlers) {
        super(getElementByTagName('html'));
        this.headTagHandler = new HeadTagHandler(tagHandlers);
        this.bodyTagHandler = new BodyTagHandler(tagHandlers);
    }

    /**
    * @public
    */
    reset() {
        this.unbindPage();
        this.headTagHandler.clearTitle();
        this.bodyTagHandler.clearAttributes();
    }




    /**
     * @public
     * @override
     * @param {Data} data 
     * @returns {Promise}
     */
    refresh(data) {
        this.data = data;
        return this.refreshWithData(data);
    }

    /**
     * @public
     * @returns {Promise}
     */
    reapply() {
        return this.refreshWithData(this.data);
    }

    /**
     * @private
     * @param {Data} data
     * @returns {Promise}
     */
    refreshWithData(data) {
        return Promise.all([
            this.headTagHandler.refresh(data),
            this.bodyTagHandler.refresh(data)
        ]);
    }

    /**
     * @public
     * @param {Page} page 
     */
    bindPage(page) {
        this.headTagHandler.clearTitle();
        this.bodyTagHandler.clearAttributes();
        this.headTagHandler.bind(page);
        this.bodyTagHandler.bindAttributes(page.bodyAttributes);
        this.bodyTagHandler.bind(page.bodyTemplate);
        this.page = page;
    }

    /**
    * @public
    * @param {Page} page
    */
    getTitle() {
        return this.headTagHandler.title.innerText;
    }

    /**
    * @private
    * @param {Page} page
    */
    unbindPage() {
        if (this.page) {
            this.headTagHandler.release(this.page.headTemplate);
            this.bodyTagHandler.release(this.page.bodyTemplate);
            this.page = undefined;
        }
    }

}