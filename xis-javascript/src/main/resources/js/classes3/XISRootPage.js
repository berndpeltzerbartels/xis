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
        this.client.loadPageModel(this);
    }

    refresh(page) {
        var head = getElementByTagName('head');
        var body = getElementByTagName('body');
        this.page.refresh(head, body);
    }

    unbindPage() {
        if (this.page) {
            this.removeBodyAttributes();
            this.removeBodyAttributes();
            var head = getElementByTagName('head');
            var body = getElementByTagName('body');
            this.page.unbindHeadContent(head);
            this.page.unbindBodyContent(body);
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