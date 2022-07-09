class XISRootPage extends XISValueHolder {

    constructor(client) {
        super(undefined);
        this.client = client;
        this.head = getElementByTagName('head');
        this.title = getElementByTagName('title');
        this.body = getElementByTagName('body');
    }

    bindPage(page) {
        this.page = page;
        this.setValues(this.client.onInitPage(this.page));
        this.setBodyAttributes(this.page);
        this.page.bindHeadContent(this.head);
        this.page.bindBodyContent(this.body);
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
        for (var attribute of page.bodyAttributes) {
            this.body.setAttribute(attribute.name, attribute.value);
        }
    }

    removeBodyAttributes() {
        for (var attribute of this.page.bodyAttributes) {
            this.body.removeAttribute(attribute.name);
        }
    }
}