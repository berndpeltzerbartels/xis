/**
 * @class MessageTagHandler
 * @extends TagHandler
 * @property {String} binding
 * @property {Expression} bindingExpression
 */
class MessageTagHandler extends TagHandler {

    /**
     * @public
     * @param {Element} tag 
     */
    constructor(tag) {
        super(tag);
        this.bindingExpression = this.expressionFromAttribute('message-for');
        this.binding = '';
    }


    /**
     * @public
     * @override
     * @param {Data} data
     */
    refresh(data) {
        this.binding = data.validationPath + '/' + this.bindingExpression.evaluate(data);
        this.refreshDescendantHandlers(data);
    }

    /**
     * @public
     * @override
     * @param {ValidatorMessages} messages 
     */
    refreshValidatorMessages(messages) {
        this.tag.innerText = messages.getMessageFor(this.binding);
        super.refreshValidatorMessages(messages);
    }


}