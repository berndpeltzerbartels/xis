class PageLinkHandler extends TagHandler {

    constructor(element) {
        super(element);
        this.element = element;
        this.urlExpression = new TextContentParser(element.getAttribute('page-link')).parse();
        if (element.localName == 'a') {
            element.setAttribute('href', '#');
        }
    }

    refresh(data) {
        var pageUrl = this.urlExpression.evaluate(data);
        this.element.onclick = e => pageController.bindPageById(pageUrl);
    }
}
