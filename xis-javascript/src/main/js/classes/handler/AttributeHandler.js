/**
* @classdesc AttributeHandler class is responsible for handling attribute values containing a variable.
* @class AttributeHandler
* @extends TagHandler
 */
class AttributeHandler extends TagHandler {

    /**
     * @param {Element} element 
     * @param {String} attrName 
     */
    constructor(element, attrName) {
        super(element);
        this.type = 'attribute-handler';
        this.attrName = attrName;
        this.attrExpression = new TextContentParser(element.getAttribute(this.attrName)).parse();
    }

    /**
     * @public
     * @override
     * @param {Data} data
     */
    refresh(data) {
        this.tag.setAttribute(this.attrName, this.attrExpression.evaluate(data));
    }
}