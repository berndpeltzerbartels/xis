class ErrorStyleHandler extends TagHandler {

    constructor(tag) {
        super(tag);
        this.type = 'error-style-handler';
        this.errorStyleClassExpression = new TextContentParser(this.tag.getAttribute('xis:error-class')).parse();
        this.bindingExpression = new TextContentParser(this.tag.getAttribute('xis:binding')).parse();
        this.lastErrorStyle = null;
    }

    refresh(data) {
        this.data = data;
        this.binding =  data.validationPath + '/' + this.bindingExpression.evaluate(data);
        this.errorClass = this.errorStyleClassExpression.evaluate(data);
        const formHandler = this.findParentFormHandler();
        formHandler.onMessageHandlerRefreshed(this, this.binding);
        return this.refreshDescendantHandlers(data);
    }

    refreshValidatorMessages(validatorMessages) {
         if (this.errorClass) {
           const errorMessage = validatorMessages.messages[this.binding];
           if (errorMessage) {
               this.lastErrorClass = this.errorClass;
               this.addCssClass(this.errorClass);
           } else if (this.lastErrorClass) {
               this.removeCssClass(this.lastErrorClass);
               this.lastErrorClass = null;
           }
       }
    }

    /**
     * @protected
     * @param {string} cssClass
     */
    addCssClass(cssClass) {
        if (!this.tag.classList.contains(cssClass)) {
            this.tag.classList.add(cssClass);
        }
    }

    reset() {
    }
}