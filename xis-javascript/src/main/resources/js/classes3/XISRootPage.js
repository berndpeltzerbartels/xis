class XISRootPage  {

    /**
     * 
     * @param {XISClient} client 
     */
    constructor(client) {
        this.client = client;
        this.head = getElementByTagName('head');
        this.title = getElementByTagName('title');
        this.body = getElementByTagName('body');
    }

    bindPage(page) {
        this.page = page;
        this.client.loadPageModel(this, this.page);
    }

    refresh(page) {
        page.bindHeadContent(this.head);
        page.bindBodyContent(this.body);
    }

    unbindPage() {
        if (this.page) {
            this.removeBodyAttributes();
            this.page.unbindHeadContent(this.head);
            this.removeBodyAttributes();
            this.page.unbindBodyContent(this.body);
            this.page = undefined;
        }
    }

    setPageTitle(page) {
        this.title.innerText = page.title;
    }

    addHeadTags(page) {
        for (var tag of page.headTags) {
            this.head.appendChild(tag);
        }
    }

    setBodyAttributes(page) {
       page.setBodyAttributes(this.body);
    }

    removeBodyAttributes() {
        this.page.removeBodyAttributes(this.body);
    }
}