/**
 * @class MessageTagHandler
 * @extends TagHandler
 * @property {String} binding
 * @property {Expression} bindingExpression
 * @property {FormElementHandler} formHandler
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
        this.formHandler = this.findParentFormHandler();
    }

    /**
     * Searches the parent form-tag and returns the handler.
     * 
     * @private
     * @returns {FormElementHandler}
     */
    findParentFormHandler() {
        var form = this.findParentFormElement();
        if (!form) throw new Error('no parent form-tag or form-tag is not bound for ' + this.tag);
        return form._handler;
    }

    /**
     * @public
     * @override
     * @param {Data} data
     */
    refresh(data) {
        this.binding = this.bindingExpression.evaluate(data);
        this.refreshDescendantHandlers(data);
    }

    /**
     * @public
     * @override
     * @param {ValidatorMessages} messages 
     */
    refreshValidatorMessages(messages) {
        this.tag.innerText = messages.getMessageFor(this.messagePath());
        super.refreshValidatorMessages(messages);
    }

    /**
     * Message path for validator message.
     * 
     * @private
     * @returns {string}
     */
    messagePath() {
        return '/' + this.formHandler.binding + '/' + this.binding;
    }
}