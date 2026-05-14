class ErrorStyleHandler extends TagHandler {

    constructor(tag) {
        super(tag);
        this.type = 'error-style-handler';
        this.errorStyleClassExpression = new TextContentParser(this.tag.getAttribute('xis:error-class')).parse();
        this.errorStyleExpression = new TextContentParser(this.tag.getAttribute('xis:error-style')).parse();
        const binding = this.tag.getAttribute('xis:error-binding') || this.tag.getAttribute('xis:binding');
        this.bindingExpression = new TextContentParser(binding).parse();
        this.lastErrorStyle = null;
        this.lastErrorClass = null;
        this.originalStyle = null;
        this.errorStyleApplied = false;
    }

    refresh(data) {
        this.data = data;
        this.binding =  data.validationPath + '/' + this.bindingExpression.evaluate(data);
        this.errorClass = this.errorStyleClassExpression.evaluate(data);
        this.errorStyle = this.errorStyleExpression.evaluate(data);
        const formHandler = this.findParentFormHandler();
        formHandler.onMessageHandlerRefreshed(this, this.binding);
        return this.refreshDescendantHandlers(data);
    }

    refreshValidatorMessages(validatorMessages) {
        const errorMessage = validatorMessages.messages[this.binding];
        if (errorMessage) {
            if (this.errorClass) {
                this.lastErrorClass = this.errorClass;
                this.addCssClass(this.errorClass);
            }
            if (this.errorStyle) {
                this.applyErrorStyle(this.errorStyle);
            }
        } else {
            this.removeLastErrorClass();
            this.removeLastErrorStyle();
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

    removeCssClass(cssClass) {
        if (this.tag.classList.contains(cssClass)) {
            this.tag.classList.remove(cssClass);
        }
    }

    removeLastErrorClass() {
        if (this.lastErrorClass) {
            this.removeCssClass(this.lastErrorClass);
            this.lastErrorClass = null;
        }
    }

    applyErrorStyle(cssText) {
        this.removeLastErrorStyle();
        this.originalStyle = this.tag.getAttribute('style');
        this.errorStyleApplied = true;
        const separator = this.originalStyle && this.originalStyle.trim().length > 0 ? '; ' : '';
        this.tag.setAttribute('style', (this.originalStyle || '') + separator + cssText);
    }

    removeLastErrorStyle() {
        if (!this.errorStyleApplied) {
            return;
        }
        if (this.originalStyle) {
            this.tag.setAttribute('style', this.originalStyle);
        } else {
            this.tag.removeAttribute('style');
        }
        this.originalStyle = null;
        this.errorStyleApplied = false;
    }

    reset() {
        this.removeLastErrorClass();
        this.removeLastErrorStyle();
    }
}
