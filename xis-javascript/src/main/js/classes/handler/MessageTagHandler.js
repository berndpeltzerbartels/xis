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
        this.bindingExpression = this.variableTextContentFromAttribute('message-for');
        this.binding = '';
    }

    reset() {
        this.tag.innerHTML = '';
    }


    /**
     * @public
     * @override
     * @param {Data} data
     */
    refresh(data) {
        this.data = data;
        this.binding = data.validationPath + '/' + this.bindingExpression.evaluate(data);
        this.refreshDescendantHandlers(data);
        this.getParentFormHandler().onMessageHandlerRefreshed(this, this.binding);
    }

    /**
     * @public
     * @override
     * @param {ValidatorMessages} messages head
     */
    refreshValidatorMessages(messages) {
        this.tag.innerText = messages.getMessageFor(this.binding);
    }


}