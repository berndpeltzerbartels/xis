class XISRootPage  {

    /**
     * 
     * @param {XISClient} client 
     */
    constructor(client) {
        this.client = client;
    }

    bindPage(page) {
        this.page = page;
        this.client.loadPageModel(this, this.page);
    }

    refresh(page) {
        page.bindHeadContent(getElementByTagName('head'));
        page.bindBodyContent(getElementByTagName('body'));
    }

    unbindPage() {
        if (this.page) {
            this.removeBodyAttributes();
            this.page.unbindHeadContent(getElementByTagName('head'));
            this.removeBodyAttributes();
            this.page.unbindBodyContent(getElementByTagName('body'));
            this.page = undefined;
        }
    }

    setPageTitle(page) {
        getElementByTagName('title').innerText = page.title;
    }

    addHeadTags(page) {
        for (var tag of page.headTags) {
            getElementByTagName('head').appendChild(tag);
        }
    }

    setBodyAttributes(page) {
       page.setBodyAttributes(getElementByTagName('title'));
    }

    removeBodyAttributes() {
        this.page.removeBodyAttributes(getElementByTagName('title'));
    }
}