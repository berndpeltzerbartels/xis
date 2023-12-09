class AttributeHandler extends TagHandler {

    /**
     * 
     * @param {Element} element 
     * @param {String} attrName 
     */
    constructor(element, attrName) {
        super(element);
        this.attrName = attrName;
        this.attrExpression = new TextContentParser(element.getAttribute(this.attrName)).parse();
    }

    refresh(data) {
        this.tag.setAttribute(this.attrName, this.attrExpression.evaluate(data));
    }
}