class HtmlTagHandler extends TagHandler {

    constructor() {
        super(getElementByTagName('html'));
        this.headTagHandler = new HeadTagHandler();
        this.bodyTagHandler = new BodyTagHandler();
    }

    /**
    * @public
    */
    reset() {
        this.headTagHandler.clearChildren();
        this.bodyTagHandler.clearChildren();
        this.headTagHandler.clearTitle();
        this.bodyTagHandler.clearAttributes();
    }


    /**
     * @public
     * @override
     * @param {Data} data 
     */
    refresh(data) {
        this.headTagHandler.refresh(data);
        this.bodyTagHandler.refresh(data);
    }

    /**
     * @public
     * @param {Page} page 
     */
    bindPage(page) {
        this.headTagHandler.clearChildren();
        this.headTagHandler.clearTitle();
        this.bodyTagHandler.clearChildren();
        this.bodyTagHandler.clearAttributes();
        this.headTagHandler.bind(page.headTemplate, page.titleExpression);
        this.bodyTagHandler.bindAttributes(page.bodyAttributes);
        this.bodyTagHandler.bind(page.bodyTemplate);
    }

    getTitle() {
        return this.headTagHandler.tag.innerText;
    }

}